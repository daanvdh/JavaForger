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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import initialization.InitDefaultValues;
import templateInput.StringConverter;

/**
 * Defines a type from an input java class which is parsed by JavaParser. This class defines common fields between {@link VariableDefinition} and
 * {@link MethodDefinition}.
 *
 * @author Daan
 */
public class TypeDefinition implements Comparable<TypeDefinition> {

  protected StringConverter name;
  /**
   * The class name representing the type of this {@link TypeDefinition}. In case of {@link MethodDefinition}s this represents the return type, can also be
   * "void". This will not contain any generics. To get the full type use ${field.fullType} instead.
   */
  protected StringConverter type;
  /** The generics of this type. For instance contains [String, Integer] if original type was Map<String, Integer>. */
  protected List<StringConverter> generics = new ArrayList<>();
  /** The imports required for this type. This list is sorted on insertion order so that template generation is consistent. */
  protected LinkedHashSet<String> typeImports = new LinkedHashSet<>();
  protected int lineNumber;
  protected int column;
  protected Set<String> annotations = new HashSet<>();
  protected Set<String> accessModifiers = new HashSet<>();
  /** The package */
  protected String pack;

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

  protected TypeDefinition(TypeDefinition.Builder<?> builder) {
    this.name = builder.name == null ? this.name : builder.name;
    this.type = builder.type == null ? this.type : builder.type;
    this.generics = builder.generics == null ? this.generics : builder.generics;
    this.typeImports = builder.typeImports == null ? this.typeImports : builder.typeImports;
    this.lineNumber = builder.lineNumber == null ? this.lineNumber : builder.lineNumber;
    this.column = builder.column == null ? this.column : builder.column;
    this.annotations = builder.annotations == null ? this.annotations : builder.annotations;
    this.accessModifiers = builder.accessModifiers == null ? this.accessModifiers : builder.accessModifiers;
    this.pack = builder.pack == null ? this.pack : builder.pack;
  }

  @Override
  public int compareTo(TypeDefinition o) {
    int line = this.getLineNumber() - o.getLineNumber();
    int column = this.getColumn() - o.getColumn();
    return line != 0 ? line : column;
  }

  public StringConverter getName() {
    return name;
  }

  public String getNameAsString() {
    return name.toString();
  }

  public void setName(String name) {
    this.name = new StringConverter(name);
  }

  public String getTypeWithoutParameters() {
    if (this.type == null || this.type.toString() == null) {
      return null;
    }
    int indexOf = type.toString().indexOf("<");
    indexOf = indexOf < 1 ? type.toString().length() : indexOf;
    String mainType = type.toString().substring(0, indexOf);
    return mainType;
  }

  public List<StringConverter> getGenerics() {
    return generics;
  }

  public void setGenerics(List<StringConverter> generics) {
    this.generics = generics;
  }

  public String getGenericsFormatted() {
    StringBuilder sb = new StringBuilder();
    if (!this.getGenerics().isEmpty()) {
      sb.append("<");
      boolean first = true;
      for (StringConverter generic : this.getGenerics()) {
        if (!first) {
          sb.append(", ");
          first = false;
        }
        sb.append(generic);
      }
      sb.append(">");
    }
    return sb.toString();
  }

  public StringConverter getStrippedType() {
    return type;
  }

  public String getNonPrimitiveType() {
    return InitDefaultValues.getObjectForPrimitive(getType().toString());
  }

  public boolean isPrimitive() {
    return InitDefaultValues.isPrimitive(type.toString());
  }

  public StringConverter getType() {
    return new StringConverter(type + getGenericsFormatted());
  }

  public void setType(String type) {
    if (type.contains("<")) {
      throw new RuntimeException("Should never contain generics");
    }
    this.type = new StringConverter(type);
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

  public String getPack() {
    return pack;
  }

  public void setPack(String pack) {
    this.pack = pack;
  }

  public String getPackage() {
    return pack;
  }

  public void setPackage(String pack) {
    this.pack = pack;
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, annotations, lineNumber, column);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("name", name).append("type", type)
        .append("annotations", annotations).append("lineNumber", lineNumber).append("column", column).append("accessModifiers", accessModifiers)
        .append("typeImports", typeImports).append("package", pack).append("generics", generics).build();
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

  @SuppressWarnings("unchecked")
  public static class Builder<T extends Builder<?>> {
    private StringConverter name;
    private StringConverter type;
    private List<StringConverter> generics = new ArrayList<>();
    private LinkedHashSet<String> typeImports;
    private Integer lineNumber;
    private Integer column;
    private Set<String> annotations = new HashSet<>();
    private Set<String> accessModifiers = new HashSet<>();
    private String pack;

    protected Builder() {
      // Builder should only be used via the parent class or extending builder
    }

    protected T copy(InitializedTypeDefinition builder) {
      this.name = builder.name == null ? this.name : builder.name;
      this.type = builder.type == null ? this.type : builder.type;
      this.generics = builder.generics == null ? this.generics : builder.generics;
      this.typeImports = builder.typeImports == null ? this.typeImports : builder.typeImports;
      this.lineNumber = builder.lineNumber;
      this.column = builder.column;
      this.annotations = builder.annotations == null ? this.annotations : builder.annotations;
      this.accessModifiers = builder.accessModifiers == null ? this.accessModifiers : builder.accessModifiers;
      this.pack = builder.pack == null ? this.pack : builder.pack;
      return (T) this;
    }

    public T name(StringConverter name) {
      this.name = name;
      return (T) this;
    }

    public T type(String type) {
      this.type = new StringConverter(type);
      return (T) this;
    }

    public T type(StringConverter type) {
      this.type = type;
      return (T) this;
    }

    public T generics(List<StringConverter> generics) {
      this.generics.clear();
      this.generics.addAll(generics);
      return (T) this;
    }

    public T genericsFromString(List<String> generics) {
      this.generics.clear();
      generics.forEach(t -> this.generics.add(new StringConverter(t)));
      return (T) this;
    }

    public T typeImports(LinkedHashSet<String> typeImports) {
      this.typeImports = typeImports;
      return (T) this;
    }

    public T lineNumber(Integer lineNumber) {
      this.lineNumber = lineNumber;
      return (T) this;
    }

    public T column(Integer column) {
      this.column = column;
      return (T) this;
    }

    public T annotations(Set<String> annotations) {
      this.annotations.clear();
      this.annotations.addAll(annotations);
      return (T) this;
    }

    public T accessModifiers(Set<String> accessModifiers) {
      this.accessModifiers.clear();
      this.accessModifiers.addAll(accessModifiers);
      return (T) this;
    }

    public T pack(String pack) {
      this.pack = pack;
      return (T) this;
    }

    public TypeDefinition build() {
      return new TypeDefinition(this);
    }

  }

}
