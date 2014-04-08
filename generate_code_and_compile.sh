#!/bin/zsh

autoload -U colors
colors

fixmldir=messages/
workdir=`pwd`/results
cd ${workdir}

function highlight () {
    echo "$fg_bold[blue]${1}$reset_color"
}

function die () {
    echo "$fg_bold[red]${*}$reset_color" 1>&2
    smiley 1
    exit 1
}

function smiley () {
    echo "      $fg_bold[blue],---."
    echo "     / $fg_bold[green]o o $fg_bold[blue]\     Oh,"
    if [[ 0 -eq $1 ]]; then
	echo "     \ $fg_bold[red]\_/ $fg_bold[blue]/    great!"
    else
	echo "     \  $fg_bold[red]O  $fg_bold[blue]/    shit!"
    fi
    echo "      \`---´$reset_color"
}

highlight "Generating code for all FIXML files in ${fixmldir}."
lein run
highlight "Code generated. Compiling now."

if [[ -d java ]]; then
    echo "* Compiling Java"
    echo " \\"
    cd java
    for dir in *; do
	echo "  |-> Compiling ${dir}"
	javac ${dir}/*.java || die "Java compile failed!"
    done
    echo "  \`---"
    echo
    cd $workdir
else
    echo "No Java to compile."
fi

if [[ -d csharp ]]; then
    echo "* Compiling C#"
    echo " \\"
    cd csharp
    for dir in *; do
	echo "  |-> Compiling ${dir}"
	mcs -target:library ${dir}/*.cs || die "C# compile failed!"
    done
    echo "  \`---"
    echo
    cd $workdir
else
    echo "No C# to compile."
fi

if [[ -d cpp ]]; then
    echo "* Compiling & Linking C++"
    echo " \\"
    cd cpp
    for dir in *; do
	echo "  |-> Compiling ${dir}"
	cd ${dir}
	g++ -std=c++0x -fpic -c *.cpp || die "C++ compile failed!"
	g++ -shared -o lib${dir}.so *.o || die "Linking failed!"
	cd -
    done
    echo "  \`---"
    echo
    cd $workdir
else
    echo "No C++ to compile."
fi

if [[ -d c ]]; then
    echo "* Compiling & Linking C"
    echo " \\"
    cd c
    for dir in *; do
	echo "  |-> Compiling & Linking ${dir}"
	cd ${dir}
	gcc -c -fpic *.c || die "C compile failed!"
	gcc -shared -o lib${dir}.so *.o || die "Linking failed!"
	cd -
    done
    echo "  \`---"
    echo
    cd $workdir
else
    echo "No C to compile."
fi

highlight "Excellent! Everything worked."
smiley 0
