# JavaForger

[![Build Status](https://travis-ci.com/daanvdh/JavaForger.svg?branch=master)](https://travis-ci.com/daanvdh/JavaForger)

JavaForger can create source code from templates using existing java classes as input. 
It reads field, methods and other class data to fill in variables in a template. 
JavaForger includes default templates for generic java code like equals, hashCode, innerBuilders and unit tests for them. 
Custom templates can be created for repetative code that is project specific. 
JavaForger is able to directly insert source code into existing classes. 

## Example
Let's say we have a class as given as below: 
	
	public class MyClass {
	  int number; 
	  String name; 
	}
	
Then calling the following code from a main class: 
	
	JavaForger.execute(DefaultConfigurations.forEquals(), "AbsolutePathTo/MyClass.java");
	
Will result in the following class:
	
	public class MyClass {
	  int number; 
	  String name; 
	
	  @Override
	  public boolean equals(Object obj) {
	    return new EqualsBuilder()
	      .append(number, other.number)
	      .append(name, other.name)
	      .isEquals();
	  }
	}
	
JavaForger requires a JavaForgerConfiguration as input together with a path to the class to use as input. 
The configuration contains the template and settings for parsing the input, processing the template and merging it to a destination class. 
The DefaultConfigurations class contains default configurations for all templates included in the project. 
A JavaForgerConfiguration can also contain child configurations, for instance DefaultConfigurations.forBuilderAndTest() has a child configuration for creating a unit test for the builder. 
That child configuration will be executed after the root configuration. 
By default the generated code for the test will be merged to the class: 'src/test/java/<custom-path>/<input-class-name>Test.java'. 
If that file does not exist, a FileNotFoundException will be thrown. 

## Setup 

Add the dependency below to the pom of your project. 

	<dependency>
	  <groupId>com.github.daanvdh.javaforger</groupId>
	  <artifactId>JavaForger</artifactId>
	  <version>2.0.0</version>
	</dependency>

To use all the features of JavaForger you also have to set the path to the project you are using as input for the templates. This is mostly needed for finding imports so that these can be used in templates, but can in the future also be needed for other things. The path has to be pointing to the source folder (for maven typically ending with src/main/java). It can be setup as follows: 

	StaticJavaForgerConfiguration.getConfig().setProjectPaths("absolutePathToProject/src/main/java");

## Settings and Dependencies 
	
This project depends on the open source template engine [FreeMarker](https://freemarker.apache.org/) for processing templates. 
Settings for executing templates with FreeMarker can be set via JavaForgerConfiguration::setFreeMarkerConfiguration. 
For processing templates FreeMarker receives a hashmap of String to Object as input. 
The string can be used inside a template to access data within the object. 
Custom parameters can be added via JavaForgerConfiguration::setInputParameters. 
Documentation on template settings and syntax can be found at [freemarker.apache.org](https://freemarker.apache.org/)
	
JavaForger depends on the open source project [JavaParser](https://github.com/javaparser/javaparser/) for reading input java classes and later inserting code into classes. 
The hashmap with input parameters is filled with the keys 'fields', 'methods' and 'class'.
The fields and methods include data like type, name and annotations which can be accessed directly from a template. 
The class contains used interfaces and extended classes. 
It is possible to make changes to the parsed data via JavaForgerConfiguration::addParameterAdjusters. 
This method receives a Consumer that accepts the hashmap with parameters. 
Inside the consumer the parameters can be changed, for instance to filter out static fields before creating a builder (e.g. DefaultAdjusters::removeStaticFields). 
The class to merge with can be changed via JavaForgerConfiguration::setMergeClass receiving the absolute path to the merge class. 
You can set the class to merge with via a provider using JavaForgerConfiguration::setMergeClassProvider. 
The provider receives either the input class path or the merged class of the parent configuration and uses that path to determine the next class path. 
An example of a merge class provider can be found in MergeClassProvider::forMavenUnitTest.

## Custom Templates

For writing custom templates you first have to set a custom template location, which can be done as follows: 
	
	StaticJavaForgerConfiguration.getConfig().addTemplateLocation("project/relative/path/to/templates");

We can define a template called "equals.javat" as given below. 
We can see that we access the fields parsed from an input class in a loop, where we use the name of each field to construct the EqualsBuilder. 
The currently supported variables are described in [TemplateVariables.md](TemplateVariables.md). 
	
	@Override
	public boolean equals(Object obj) {
	  return new EqualsBuilder()
	<#list fields as field>
	    .append(${field.name}, other.${field.name})
	</#list>
	    .isEquals();
	}

## Features

- Generated class code will be merged recursively. 
- By default existing code (fields, constructors, methods) will not be overridden, setting via JavaForgerConfiguration::setOverride. 
- By default code will be inserted in the following order; a) fields, constructors, methods, classes b) public, 'no access modifier', protected, private. This can be overridden via StaticJavaForgerConfiguration::setMerger. 
- Required imports for any field f in a template can be added by: 

	<#list f.typeImports as import>
	import ${import};
	</#list>
	
- Initialization values for a field f are unique per type and can used by: 

	${field.type} ${field.name} = ${field.init1};

## Roadmap

- Create eclipse plugin to execute customly created templates without having to specify the input class manually. 
- Automatically format java classes after inserting code.
- Create data flow graph for simple methods to be able to generate unit test code. 

## License

JavaForger is available under the terms of the Apache License. http://www.apache.org/licenses/LICENSE-2.0
