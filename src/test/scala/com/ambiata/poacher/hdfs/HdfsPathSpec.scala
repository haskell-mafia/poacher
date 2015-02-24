package com.ambiata.poacher.hdfs

import org.specs2.Specification

import com.ambiata.disorder._
import com.ambiata.mundane.io._
import com.ambiata.mundane.io.Arbitraries._
import com.ambiata.mundane.io.MemoryConversions._
import com.ambiata.mundane.path._
import com.ambiata.mundane.path.Arbitraries._
import com.ambiata.poacher.hdfs.Arbitraries._
import com.ambiata.poacher.hdfs.HdfsMatcher._

import org.apache.hadoop.conf.Configuration

import scala.io.Codec

import org.specs2._
import org.specs2.matcher.DisjunctionMatchers
import scalaz._, Scalaz._, effect.Effect._

class HdfsPathSpec extends Specification with ScalaCheck with DisjunctionMatchers { def is = s2"""


 HdfsPath
 ========

  HdfsPath operations should have the symenatics as Path operations

    ${ prop((l: Path, p: Path) => (HdfsPath(l) / p).path ==== l / p) }

    ${ prop((l: Path, p: Path) => (HdfsPath(l).join(p)).path ==== l.join(p)) }

    ${ prop((l: Path, p: Component) => (HdfsPath(l) | p).path ==== (l | p)) }

    ${ prop((l: Path, p: Component) => (HdfsPath(l).extend(p)).path ==== l.extend(p)) }

    ${ prop((l: Path, p: S) => (HdfsPath(l) /- p.value).path ==== l /- p.value) }

    ${ prop((l: Path, p: Component) => (HdfsPath(l) | p).rebaseTo(HdfsPath(l)).map(_.path) ==== (l | p).rebaseTo(l)) }

    ${ prop((l: Path) => HdfsPath(l).dirname.path ==== l.dirname) }

    ${ prop((l: Path) => HdfsPath(l).basename ==== l.basename) }

 HdfsPath IO
 ===========

  HdfsPath should be able to determine files, directories and handle failure cases

    ${ HdfsTemporary.random.path.flatMap(path => path.touch >> path.determine.map(_ must beFile)) }

    ${ HdfsTemporary.random.path.flatMap(path => path.mkdirs >> path.determine.map(_ must beDirectory)) }

    ${ HdfsTemporary.random.path.flatMap(path => path.determine.map(_ must beNone)) }

    ${ HdfsPath(Path("")).determine.map(_ must beNone) }

  HdfsPath can determine a file and handle failure cases

    ${ HdfsTemporary.random.path.flatMap(path => path.touch >> path.determineFile) must beOk }

    ${ HdfsTemporary.random.path.flatMap(path => path.mkdirs >> path.determineFile) must beFailWithMessage("Not a valid file") }

    ${ HdfsTemporary.random.path.flatMap(path => path.determineFile) must beFailWithMessage("Not a valid File or Directory") }

  HdfsPath can determine a directory and handle failure cases

    ${ HdfsTemporary.random.path.flatMap(path => path.touch >> path.determineDirectory) must beFailWithMessage("Not a valid directory") }

    ${ HdfsTemporary.random.path.flatMap(path => path.mkdirs >> path.determineDirectory) must beOk }

    ${ HdfsTemporary.random.path.flatMap(path => path.determineDirectory) must beFailWithMessage("Not a valid File or Directory") }



"""
  val beFile = beSome(be_-\/[HdfsFile])
  val beDirectory = beSome(be_\/-[HdfsDirectory])

  implicit val BooleanMonoid: Monoid[Boolean] =
    scalaz.std.anyVal.booleanInstance.conjunction
}
