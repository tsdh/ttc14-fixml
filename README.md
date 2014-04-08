# ttc14-fixml

This is the [FunnyQT](http://jgralab.github.io/funnyqt/) solution to the FIXML
code generation case of the
[Transformation Tool Contest 2014](http://www.transformation-tool-contest.eu/).

## Usage

The complete transformation on all provided test model (and some additional
ones) can be triggered by running the `generate_code_and_compile.sh` shell
script.  It will generate the code for each message in the languages Java, C\#,
C++, and C.  The generated code resides in directories named
`results/<lang>/<msg_name>/`.

## License

Copyright © 2014 Tassilo Horn <horn@uni-koblenz.de>

Distributed under the GNU General Public License, version 3 (or later).
