/*
 * Copyright 2018 by Daan van den Heuvel.
 *
 * This file is part of JavaForger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package merger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.Provider;
import com.github.javaparser.Providers;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import freemarker.template.TemplateException;
import generator.CodeSnipit;
import generator.Generator;
import generator.JavaForgerConfiguration;
import reader.Parser;

/**
 * Class for merging generated {@link CodeSnipit}s into java lass files.
 *
 * @author Daan
 */
public class CodeSnipitMerger {

  /**
   * Merges the input {@link CodeSnipit} with the mergeClass given by the {@link JavaForgerConfiguration}. Currently only codeSnipits are supported that are not
   * a complete class. Imports are also not supported. Inside this method we wrap the code within the codeSnipit in a class and let {@link JavaParser} read it.
   * Then everything inside the wrapped class will be inserted into the inputClass. Variables with the same name and Methods with the same name and signature
   * will be replaced in the mergeClass. Changes will be written directly in the mergeClass.
   *
   * @param config The {@link JavaForgerConfiguration} containing merge settings and the path of the class to merge with.
   * @param codeSnipit The {@link CodeSnipit} which will be merged into the input class.
   * @throws IOException
   */
  public void merge(JavaForgerConfiguration config, CodeSnipit codeSnipit, String mergeClassPath) throws IOException {
    if (config.getMergeClassProvider() == null) {
      return;
    }
    CompilationUnit existingCode = read(mergeClassPath);
    CompilationUnit newCode = read(codeSnipit);
    merge(existingCode, newCode);
    write(mergeClassPath, existingCode);
    format(config, mergeClassPath);
  }

  private void format(JavaForgerConfiguration config, String mergeClassPath) {
    System.err.println("formatting is not yet supported and should be done manually for class " + mergeClassPath);
    // TODO Probably best to call the formatter via the cmd:
    // https://stackoverflow.com/questions/15464111/run-cmd-commands-through-java
    // https://www.beyondjava.net/run-eclipse-formatter-command-line

    // TODO Probably we have to let the user set the path to the desired java-formatter in the config
  }

  private void merge(CompilationUnit existingCode, CompilationUnit newCode) {
    mergeImports(existingCode, newCode);
    NodeList<BodyDeclaration<?>> existingMembers = getParent(existingCode).getMembers();
    NodeList<BodyDeclaration<?>> newMembers = getParent(newCode).getMembers();
    for (BodyDeclaration<?> member : newMembers) {
      int replacementIndex = findReplacementNode(existingMembers, member);
      if (replacementIndex >= 0) {
        existingMembers.set(replacementIndex, member);
      } else {
        int insertIndex = findInsertionLocation(existingMembers, member);
        existingMembers.add(insertIndex, member);
      }
    }
  }

  private void mergeImports(CompilationUnit existingCode, CompilationUnit newCode) {
    Set<ImportDeclaration> existingSet = new HashSet<>(existingCode.getImports());
    newCode.getImports().stream().filter(imp -> !existingSet.contains(imp)).forEach(existingCode::addImport);
  }

  private int findInsertionLocation(NodeList<BodyDeclaration<?>> existingMembers, BodyDeclaration<?> member) {
    int index = 0;
    for (int i = 0; i < existingMembers.size(); i++) {
      if (existingMembers.get(i).getClass().equals(member.getClass())) {
        Optional<EnumSet<Modifier>> modNew = findModifiers(member);
        Optional<EnumSet<Modifier>> modExist = findModifiers(existingMembers.get(i));
        if (modNew.isPresent() && modExist.isPresent()) {
          if (hasHigherPriorityModifier(modExist.get(), modNew.get())) {
            index = i + 1;
          }
        } else {
          index = i + 1;
        }
      }
    }
    return index;
  }

  private boolean hasHigherPriorityModifier(EnumSet<Modifier> modExist, EnumSet<Modifier> modNew) {
    boolean hasHigherPrio;
    if (modExist.contains(Modifier.PUBLIC)) {
      hasHigherPrio = true;
    } else if (modExist.contains(Modifier.PROTECTED)) {
      hasHigherPrio = !modNew.contains(Modifier.PUBLIC) && !isDefaultModifier(modNew);
    } else if (modExist.contains(Modifier.PRIVATE)) {
      hasHigherPrio = modNew.contains(Modifier.PRIVATE);
    } else {
      hasHigherPrio = !modNew.contains(Modifier.PUBLIC);
    }
    return hasHigherPrio;
  }

  private boolean isDefaultModifier(EnumSet<Modifier> modifiers) {
    return !modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PROTECTED) && !modifiers.contains(Modifier.PRIVATE);
  }

  private Optional<EnumSet<Modifier>> findModifiers(BodyDeclaration<?> member) {
    EnumSet<Modifier> modifiers = null;
    if (FieldDeclaration.class.isAssignableFrom(member.getClass())) {
      FieldDeclaration d = (FieldDeclaration) member;
      modifiers = d.getModifiers();
    } else if (CallableDeclaration.class.isAssignableFrom(member.getClass())) {
      CallableDeclaration<?> d = (CallableDeclaration<?>) member;
      modifiers = d.getModifiers();
    } else if (TypeDeclaration.class.isAssignableFrom(member.getClass())) {
      TypeDeclaration<?> d = (TypeDeclaration<?>) member;
      modifiers = d.getModifiers();
    }
    return Optional.ofNullable(modifiers);
  }

  private int findReplacementNode(NodeList<BodyDeclaration<?>> existingMembers, BodyDeclaration<?> member) {
    int index = -1;
    for (int i = 0; i < existingMembers.size(); i++) {
      BodyDeclaration<?> exists = existingMembers.get(i);
      if (exists.getClass().equals(member.getClass())) {

        // TODO add a setting what should happen in case stuff is the same. Options: replace, ignore, print error

        if (memberIsReplacement(exists, member)) {
          index = i;
        }
      }
    }
    return index;
  }

  /**
   * Checks for different types of {@link BodyDeclaration}s if the input member is the replacement for the existing member. For {@link MethodDeclaration} this
   * is done based on the method signature.
   *
   * @param exists The existing member.
   * @param member The member for which we need to check if is is the replacement.
   * @return
   */
  private boolean memberIsReplacement(BodyDeclaration<?> exists, BodyDeclaration<?> member) {
    boolean isReplacement = false;

    if (MethodDeclaration.class.isAssignableFrom(member.getClass())) {
      MethodDeclaration m1 = (MethodDeclaration) exists;
      MethodDeclaration m2 = (MethodDeclaration) member;
      isReplacement = m1.getName().equals(m2.getName());
      // TODO check if we can do equality on parameter types.
      List<Type> parameterTypes1 = m1.getParameters().stream().map(p -> p.getType()).collect(Collectors.toList());
      List<Type> parameterTypes2 = m2.getParameters().stream().map(p -> p.getType()).collect(Collectors.toList());
      isReplacement = isReplacement && parameterTypes1.equals(parameterTypes2);
    } else if (FieldDeclaration.class.isAssignableFrom(member.getClass())) {
      FieldDeclaration f1 = (FieldDeclaration) exists;
      FieldDeclaration f2 = (FieldDeclaration) member;
      isReplacement = f1.getVariable(0).getName().asString().equals(f2.getVariable(0).getName().asString());
    } else {
      System.err
          .println("The type " + member.getClass().getName() + " is currently not supported. This type will not be replaced even though it already exists. ");
    }
    return isReplacement;
  }

  private ClassOrInterfaceDeclaration getParent(CompilationUnit cu) {
    ClassOrInterfaceDeclaration parentNode = null;
    for (TypeDeclaration<?> type : cu.getTypes()) {
      if (type instanceof ClassOrInterfaceDeclaration) {
        parentNode = (ClassOrInterfaceDeclaration) type;
        break;
      }
    }
    if (parentNode == null) {
      System.err.println("Parent node was not found in Compilation Unit below:");
      System.err.println(cu.toString());
    }
    return parentNode;
  }

  protected void write(String className, CompilationUnit existingCode) throws IOException {
    // If LexicalPreservingPrinter fails we don't want the file to get lost.
    String backupFile = readFile(className, StandardCharsets.UTF_8);
    try (PrintWriter writer = new PrintWriter(className, "UTF-8")) {
      write(existingCode, writer);
      writer.close();
    } catch (Exception e) {
      try (PrintWriter writer2 = new PrintWriter(className, "UTF-8")) {
        writer2.append(backupFile);
        writer2.close();
      }
      e.printStackTrace();
    }
  }

  // protected so that we can overwrite this to throw an exception in the unit test.
  protected void write(CompilationUnit existingCode, PrintWriter writer) throws IOException {
    LexicalPreservingPrinter.print(existingCode, writer);
  }

  private String readFile(String path, Charset encoding) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, encoding);
  }

  private CompilationUnit read(CodeSnipit codeSnipit) {
    // TODO make this flexible so that we only add the class if needed
    String string = codeSnipit.toString();
    int index = firstIndexAfterImports(string);
    String code = string.substring(0, index) + "public class ClassToMerge {\n" + string.substring(index) + "\n}";
    CompilationUnit cu = Parser.parse(code);
    // Needed to preserve the original formatting
    LexicalPreservingPrinter.setup(cu);
    return cu;
  }

  private int firstIndexAfterImports(String string) {
    int lineBegin = 0;
    int lineEnd = string.indexOf(";");
    lineEnd = lineEnd < 0 ? string.length() : (lineEnd + 1);
    String importDeclaration = string.substring(lineBegin, lineEnd);
    ParseResult<ImportDeclaration> result = parseImport(importDeclaration);

    while (result.isSuccessful()) {
      lineBegin = lineEnd + 1;
      lineEnd = lineBegin + string.substring(lineBegin).indexOf(";");
      lineEnd = lineEnd < 0 ? string.length() : (lineEnd + 1);
      importDeclaration = string.substring(lineBegin, lineEnd);
      result = parseImport(importDeclaration);
    }
    return lineBegin;
  }

  private ParseResult<ImportDeclaration> parseImport(String importDeclaration) {
    JavaParser parser = new JavaParser();
    ParseStart<ImportDeclaration> parseStart = ParseStart.IMPORT_DECLARATION;
    Provider provider = Providers.provider(importDeclaration);
    ParseResult<ImportDeclaration> result = parser.parse(parseStart, provider);
    return result;
  }

  private CompilationUnit read(String className) throws IOException {
    CompilationUnit cu = null;
    try (FileInputStream in = new FileInputStream(className)) {
      cu = JavaParser.parse(in);
      in.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    // Needed to preserve the original formatting
    LexicalPreservingPrinter.setup(cu);
    return cu;
  }

  public static void main(String[] args) throws IOException, TemplateException {
    String inputClass = "src/test/java/inputClassesForTests/Product.java";
    String template = "equals.javat";

    JavaForgerConfiguration config =
        JavaForgerConfiguration.builder().withoutModifiers(Modifier.STATIC).withTemplate(template).withMergeClass(inputClass).build();

    new Generator().execute(config, inputClass).toString();
  }

}
