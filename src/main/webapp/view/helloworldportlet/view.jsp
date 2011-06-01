<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<portlet:defineObjects />

<h1>Hello ${username} !!!!!</h1>

<portlet:actionURL name="submitUsername" var="saveUrl" />
<portlet:renderURL portletMode="VIEW" var="cancelUrl" />

<form action="${saveUrl}" method="post">
    ${form}
</form>
