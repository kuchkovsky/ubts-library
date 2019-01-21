#!/usr/bin/env scalas

/***
scalaVersion := "2.12.8"

libraryDependencies += "com.healthmarketscience.jackcess" % "jackcess" % "2.2.2"
  */

import java.io.File

import com.healthmarketscience.jackcess.DatabaseBuilder

val table = DatabaseBuilder.open(new File("Bazastara.mdb")).getTable("Book")

print(table)
