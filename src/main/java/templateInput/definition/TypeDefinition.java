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
package templateInput.definition;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import initialization.VariableInitializer;
import templateInput.StringConverter;

/**
 * Defines a type from an input java class which is parsed by JavaParser. This class defines common fields between {@link VariableDefinition} and
 * {@link MethodDefinition}.
 *
 * @author Daan
 */
public class TypeDefinition implements Comparable<TypeDefinition> {

  protected String name;
  protected String type;
  /** The imports required for this type. This list is sorted on insertion order so that template generation is consistent. */
  protected LinkedHashSet<String> typeImports = new LinkedHashSet<>();
  protected int lineNumber;
  protected int column;
  protected Set<String> annotations = new HashSet<>();
  protected Set<String> accessModifiers = new HashSet<>();

  public TypeDefinition() {
    // Make default constructor visable.
  }

  /**
   * Copy constructor
   *
   * @param type
   */
  public TypeDefinition(TypeDefinition type) {
    this.name = type.name;
    this.type = type.type;
    this.lineNumber = type.lineNumber;
    this.column = type.column;
    this.annotations = type.annotations;
    this.accessModifiers = type.accessModifiers;
  }

  @Override
  public int compareTo(TypeDefinition o) {
    int line = this.getLineNumber() - o.getLineNumber();
    int column = this.getColumn() - o.getColumn();
    return line != 0 ? line : column;
  }

  public String getName() {
    return name;
  }

  public StringConverter getName_() {
    return new StringConverter(name);
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public String getNonPrimitiveType() {
    return VariableInitializer.getObjectForPrimitive(type);
  }

  public boolean isPrimitive() {
    return VariableInitializer.isPrimitive(type);
  }

  public StringConverter getType_() {
    return new StringConverter(type);
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public void setLineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public int getColumn() {
    return column;
  }

  public void setColumn(int column) {
    this.column = column;
  }

  public Set<String> getAnnotations() {
    return annotations;
  }

  public void setAnnotations(Set<String> annotations) {
    this.annotations = annotations;
  }

  public Set<String> getAccessModifiers() {
    return accessModifiers;
  }

  public void setAccessModifiers(Set<String> accessModifiers) {
    this.accessModifiers = accessModifiers;
  }

  public LinkedHashSet<String> getTypeImports() {
    return typeImports;
  }

  public void addTypeImport(String typeImport) {
    this.typeImports.add(typeImport);
  }

  // Only sorted collections may be input for the sorted typeImports
  public void addTypeImports(List<String> imports) {
    this.typeImports.addAll(imports);
  }

  // Only sorted collections may be input for the sorted typeImports
  public void addTypeImports(LinkedHashSet<String> imports) {
    this.typeImports.addAll(imports);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, annotations, lineNumber, column);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("name", name).append("type", type)
        .append("annotations", annotations).append("lineNumber", lineNumber).append("column", column).append("accessModifiers", accessModifiers)
        .append("typeImports", typeImports).build();
  }

  @Override
  public boolean equals(Object obj) {
    boolean equals = false;
    if (this == obj) {
      equals = true;
    } else if (obj != null && getClass() == obj.getClass()) {
      TypeDefinition other = (TypeDefinition) obj;
      equals = new EqualsBuilder().append(name, other.name).append(type, other.type).append(lineNumber, other.lineNumber).append(column, other.column)
          .append(annotations, other.annotations).append(accessModifiers, other.accessModifiers).append(typeImports, other.typeImports).isEquals();
    }
    return equals;
  }

}
