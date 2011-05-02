<h4>Recent tags:
<g:each var="tag" in="${recentTags}" status="index">
	<g:link mapping="taggedUpload" params="[tag:tag]">${tag}</g:link>
</g:each>
</h4>