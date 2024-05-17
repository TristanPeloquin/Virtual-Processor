# Virtual Processor
The virtual processor is a essentially a simulation of a real processor, going as far as to use bits/words to represent data.
Packaged with this processor is also a lexer/parser that allows the user to run their own code using a unique instruction set.
This means that fully fledged programs can be written using this processor and can do most of what a real processor can do.

Of course, there are some caveats, such as performance, which was sacrificed in order to stay true to the actual functioning of a processor,
as the end goal of this project was to derive a deeper understanding of the inner workings of a processor (without actually building one).

## Usage
To use the processor, first launch/install your IDE of choice - for reference, I used IntelliJ, though others should work just fine - with JDK 17
installed (other versions may also work). Create a new project in said IDE, and drop the files from this repository in. From here I recommend running Main.java through your IDE
with custom arguments, such as here:

![image](https://github.com/TristanPeloquin/Virtual-Processor/assets/98565896/f17b4a8a-7bac-467b-a415-ec0436baaecf)

If unable to, then you can run "javac Main.java" and then "java Main [insert file name]" from the terminal, replacing [insert file name] with the name of your text file
containing your code. Provided with the java files is a sample "code.txt" file which contains a very simple program that your can edit at will.

The processor is properly working if you get something like the following as output:

![image](https://github.com/TristanPeloquin/Virtual-Processor/assets/98565896/50722406-40c5-4760-a0cd-cfe6c87e8978)

The output is showing the state of the registers at each processor cycle, and as you can see they are being modified through our program. From here feel free to try out
your own code and run your own programs.

For further documentation, see the following document for the instruction set used:[SIA32.pdf](https://github.com/TristanPeloquin/Virtual-Processor/files/15358273/SIA32.pdf)

The code is also extensively commented/documented if you want to make any modifications.
