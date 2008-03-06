<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.hillsdon.net/ns/reviki/tags" prefix="sw" %>

<tiles:insertTemplate template="SiteTemplate.jsp">
  <tiles:putAttribute name="title">Wiki List</tiles:putAttribute>
  <tiles:putAttribute name="heading">Wiki List</tiles:putAttribute>
  <tiles:putAttribute name="content">
    <ul id="wikiList">
      <c:forEach var="wikiName" items="${configuration.wikiNames}">
        <li>
          <a href="<c:url value="/pages/${wikiName}/FrontPage"/>"><c:out value="${wikiName}"/></a>
          <c:if test="${wikiName == configuration.defaultWiki}">(also available at the <a href="<c:url value="/pages/FrontPage"/>">default location</a>)</c:if>
        </li>
      </c:forEach>
    </ul>
    <c:if test="${empty configuration.wikiNames}">
    <p>
    There are no wikis configured yet.
    </p>
    </c:if>
    <p>
    To configure a new wiki just go to the URL of one of its pages
    and fill in the configuration details.
    </p>
    <p>
    Enter a wiki name below to go to the FrontPage for that wiki. 
    </p>
    <form id="jump" name="jump" action="<c:url value="/jump"/>">
    <input type="text" name="name"/><input type="submit" name="go" value="Go"/>
    </form>
  </tiles:putAttribute>
</tiles:insertTemplate>
