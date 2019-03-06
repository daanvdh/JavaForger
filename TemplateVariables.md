
## Template Variables

The variable below are accessible directly within a template, with the FreeMarker syntax. The value of a template can be used with the syntax ${value}. Elements within a list can be accessed with the syntax <#list values as value> ... </#list>. Subvalues can be used by concatenating it with a single dot; ${value.subvalue}. More advanced expressions like conditionals can be found at [freemarker.apache.org](https://freemarker.apache.org/). 

```
|- class		ClassDefinition containing data defined by a class. 
|- fields		list<VariableDefinition>, usage: <#list fields as field> ... </#list>
|- constructors		list<MethodDefinition>, usage: <#list constructors as constructor> ... </#list>
|- methods		list<MethodDefinition>, usage: <#list methods as method> ... </#list>
|- package		The package of the class to which the code will be merged
```
The fields, constructors and methods of a class are accessible directly as well as via the class. This is to support both easy access as well as using multiple classes as input for a template. The types VariableDefinition and MethodDefinition are described below. Note that a constructor is modelled as a method where the type and the name are equal. 


```
ClassDefinition
|- extend		The extended class, can be null and should be checked via <#if class.extend??> ... </#if>
|- interfaces		List<String> containing the interfaces implemented by this class. 
|- fields		list<TypeDefinition>, usage: <#list class.fields as field> ... </#list>
|- constructors		list<MethodDefinition>, usage: <#list class.constructors as constructor> ... </#list>
|- methods		list<MethodDefinition>, usage: <#list class.methods as method> ... </#list>
|- *TypeDefinition	The accessible Name and Type fields of the class are descibed under VariableDefinition. 

MethodDefinition
|- parameters		List<TypeDefinition> containing the input parameters. 
|- *TypeDefinition	The accessible Name and Type fields of the class are descibed under TypeDefinition. 

TypeDefinition
|- name			Name of the method, conversion of this name possible via StringConverter
|- type 		Type of the variable or method return type, conversion possible via StringConverter.
|- nonPrimitiveType 	The non-primitive version of the type, gives the same type if it's already non-primitive. 
|- typeImports		List<String> containing the java imports required for this type. 
|- lineNumber		The line number where this type starts
|- column		The column where this type starts
|- annotations		List<String> containing the annotations for this type
|- accessModifiers 	List<String> containing the access modifiers
|- defaultInit		The default initialization for the current type, especially used for initializing empty collections. 
|- init1		An initialisation value for the current type. 
|- init2		An initialisation value for the current type, distinct from init1. 
|- noInit		Contains the value for this type if it is not initialized. 
|- collection		Boolean indicating if the type extends collection. 
|- initImports		List<String> containing the java imports required for using the initialisation values. 

StringConverter
|- lower		Lower case all characters. 
|- upper		Upper case all characters. 
|- lowerFirst		Lower case the first character. 
|- upperFirst		Upper case the first character. 
|- snakeCase		Snake case the characters. 
|- lowerSpace		Add spaces before each uppercase character and then lower case all. 

```
