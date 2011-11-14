package org.pettswood

import xml._

class Parser(domain: DomainBridge) {

  def parse(node: Node): Node = {
    node match {
      case elem: Elem => elem.label match {
        case "table" => domain.table(firstCell(elem).text); deepCopy(elem)
        case "tr" => domain.row(); deepCopy(elem)
        case "td" if((elem \\ "table").iterator.hasNext) => <td>{new Parser(domain.nestedDomain()).parse(<div>{NodeSeq.fromSeq(elem.child)}</div>)}</td>
        case "td" => val result = domain.cell(elem.text); deepCopy(elem, cssAdder(result.name), contentFor(elem.text, result))
        case _ => deepCopy(elem)
      }
      case any => any
    }
  }

  def deepCopy(elem: Elem, attributes: (Elem) => MetaData = _.attributes, extraContent: NodeSeq = NodeSeq.Empty): Elem =
    // TODO - use extraContent.headOption
    elem.copy(elem.prefix, elem.label, attributes(elem), TopScope, if (extraContent.iterator.hasNext) extraContent.head +: parsedChildren(elem) else parsedChildren(elem))

  def cssAdder(className: String): (Elem) => MetaData = {
    (elem: Elem) => {
      val classes = (elem \ "@class").text match {
        case "" => className
        case x => x + " " + className
      }
      new UnprefixedAttribute("class", classes, Null)
    }
  }

  def contentFor(expectedText: String, result: Result) = {
    result match {
      case Fail(actual) => <span class="result">{actual}<br></br>but expected:<br></br></span>
      case Exception(exceptionText) => <span class="result">{exceptionText}<br></br>Expected:<br></br></span>
      case _ => NodeSeq.Empty
    }
  }

  def parsedChildren(node: Node): NodeSeq = NodeSeq.fromSeq(node.child).map(child => parse(child))
  def firstCell(nodeSeq: NodeSeq): Elem = (nodeSeq \\ "td").head match { case elem: Elem => elem }
}