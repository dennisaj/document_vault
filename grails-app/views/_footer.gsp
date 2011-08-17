<g:if test="${g.meta(name:'environment.BUILD_NUMBER')}">
	<div id="build-number" class="hidden">Build #<g:meta name="environment.BUILD_NUMBER" /></div>
</g:if>
<g:if test="${g.meta(name:'build.date')}">
	<div id="build-date" class="hidden">Built on <g:meta name="build.date" /></div>
</g:if>
