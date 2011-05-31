<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<portlet:defineObjects />

<h1>Hello World !!!!!</h1>

<portlet:actionURL name="submitUsername" var="saveUrl" />
<portlet:renderURL portletMode="VIEW" var="cancelUrl" />

<c:out value="${username}" /><br/>

<a href="${min}">min</a><br/>
<a href="${normal}">normal</a><br/>
<a href="${max}">max</a><br/>

<form action="${saveUrl}" method="post">
    <table>
        <tr>
            <td>User Name</td>
            <td>
                <input type="text" name="username"/>
            </td>
        </tr>
        <tr>
            <td>
                <input type="submit" label="Save"/>
            </td>
        </tr>
    </table>
</form>
