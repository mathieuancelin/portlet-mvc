<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<portlet:defineObjects />

<h1>Edit preferences</h1>

<portlet:actionURL name="savePreferences" var="saveUrl" />
<portlet:renderURL portletMode="VIEW" var="cancelUrl" />
<c:if test="${!empty error}">
    <span class="portlet-msg-error">${error}</span>
</c:if>
<form action="${saveUrl}" method="post">
    <table>
        <tr>
            <td>Show uppercase : </td>
            <td>
                <input type="text" name="upper" value="${uppercase}" />
            </td>
        </tr>
        <tr>
            <td>
                <input type="submit" label="Save"/>
            </td>
        </tr>
    </table>
</form>