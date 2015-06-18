# jar2uml
Jar2UML is an Eclipse plugin that imports and converts Java jar files into a UML model in the workspace. It is used to reverse engineer Java API class libraries for PlatformKit's Java platform model.

Jar2UML has two import modes. The standard mode imports the contents of the selected Jar file(s) and the references made by the Jar file(s). The dependency mode imports only the references. The dependency mode can be used when Extracting Platform Dependencies of Third-party Components using the PlatformKit Eclipse Plugin.

Apart from jar files, Jar2UML also supports converting class files in Eclipse Java projects, and it understands the zip, war, ear, rar, and sar formats.
