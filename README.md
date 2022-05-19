# Annotated 2
Annotated is a development tool for Minecraft Forge mods.

It is an annotation processor that can generate network channels and packets for you.

## Features

### Generating a channel

Adding a channel annotation to a class will create a network channel associated with the class using the selected channel:  
* The ``@GenerateChannel`` annotation will generate a new SimpleChannel with the given name and version.
* The ``@DelegateChannel`` annotation will use the channel defined using ``@GenerateChannel`` in the given class.

### Generating a packet

Annotated allows you to define packets very quickly and easily.  
To do so, first create in your network class the function you want to call on the target side.
Annotate it with either ``@ClientPacket`` if the target is the client side or ``@ServerPacket`` if it is the server side.  
A new packet class is generated and is automatically registered to the network.
The class contains a ``send`` method that takes in parameters a target and parameters and that will send the packet to the target. Once the packet is received on the target, the annotated function is called with the parameters passed to ``send``.
Client packets contain a ``sendTo`` method to send the packet to a single player and server packets contain ``sendToServer`` to send the packet to the server.

Some parameter types of the annotated method have special meanings, won't be required in the ``send`` function and a value will automatically be assigned:  
* ``ServerPlayerEntity``: the sender of the packet
* ``NetworkEvent.Context``: the context of the packet

## Gradle setup

### With every IDE

* Add the JitPack repository in your build.gradle:

```groovy
repositories {
    maven {
        name = 'JitPack'
        url = 'https://jitpack.io'
    }
}
```

* Add Annotated to your dependencies:

```groovy
compileOnly 'com.github.LeBossMax2:Annotated:MC1.16.4-v2.0'
annotationProcessor 'com.github.LeBossMax2:Annotated:MC1.16.4-v2.0'
```

### With Eclipse

Eclipse requires addition configuration. Using the ``com.diffplug.eclipse.apt`` plugin is recommended to configure it more easily:

```groovy
plugins {
    id 'com.diffplug.eclipse.apt' version '3.36.2'
}
```

The output path of the annotation processor must be configured like so:

```groovy
eclipse {
	// Set the location of the generated source files
	jdt.apt.genSrcDir = file('src-gen/main/java')
	jdt.apt.genTestSrcDir = file('src-gen/test/java')

	// Set the output of the generated classes
	classpath.file.whenMerged {
		entries.findAll { it.kind == 'output' } *.path = 'bin/main'
	}
}
```

Then, add the Minecraft libraries to the path for the annotation processor:

```groovy
configurations {
    annotationProcessor.extendsFrom minecraft
}
```

Finally running ``./gradlew eclipseJdtApt eclipseJdt eclipseFactorypath`` will configure eclipse to use the annotation processor.
