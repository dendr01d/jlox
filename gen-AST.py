# This script will generate a file called "Expr.java"
# Which will contain all the code for defining the expressions used in Lox
# It's easier to just programmatically generate them

import sys

def main():
	# the first argument of sys.argv is the script's own name
	if (len(sys.argv) != 2):
		print("Usage: gen-AST.py <output-directory>")
		sys.exit(1)
	
	outputDir = sys.argv[1]

	defineAST(outputDir, "Expr", [
		"Binary   : Expr left, Token operator, Expr right",
		"Grouping : Expr expression",
		"Literal  : Object value",
		"Unary    : Token operator, Expr right"])


def defineAST(outputDir, baseName, types):
	path = outputDir + "/" + baseName + ".java"
	f = open(path, 'w')

	f.write("package jlox;\n\n")
	f.write("import java.util.List;\n\n")
	f.write("abstract class " + baseName + " {\n")

	defineVisitor(f, baseName, types)

	for t in types:
		className = t.split(':')[0].strip()
		fields = t.split(':')[1].strip()
		defineType(f, baseName, className, fields)
	
	f.write("\tabstract <R> R accept(Visitor<R> visitor);\n")

	f.write("}\n\n")
	f.close()


def defineVisitor(f, baseName, types):
	f.write("\tinterface Visitor<R> {\n")

	for t in types:
		typeName = t.split(':')[0].strip()
		f.write("\t\tR visit" + typeName + baseName + "(" +
			typeName + " " + baseName.lower() + ");\n")

	f.write("\t}\n\n")


def defineType(f, baseName, className, fieldList):
	# declare
	f.write("\tstatic class " + className + " extends " + baseName + " {\n")
	# constructor
	f.write("\t\t" + className + "(" + fieldList + ") {\n")
	
	# store params in fields
	fields = fieldList.split(", ")
	for field in fields:
		name = field.split(" ")[1]
		f.write("\t\t\tthis." + name + " = " + name + ";\n")
	
	f.write("\t\t}\n\n")
	
	f.write("\t\t<R> R accept(Visitor<R> visitor) {\n")
	f.write("\t\t\treturn visitor.visit" + className + baseName + "(this);\n")
	f.write("\t\t}\n\n")

	# fields
	for field in fields:
		f.write("\t\tfinal " + field + ";\n")

	f.write("\t}\n\n")



if __name__ == "__main__":
	main()
