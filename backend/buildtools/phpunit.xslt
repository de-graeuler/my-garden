<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!--
PHPUnit logfile XLST template

This fileset provides an easy way to view the PHPUnit XML (JUnit) logfiles in a human readable manner using a web browser.

Use this either in combination with the accompanying html file or add the following tag straight
after the xml opening tag of the logfile:
<?xml-stylesheet type="text/xsl" href="phpunit.xslt"?>

The thresholds used for the colour-coding can be changed by adjusting the variables at the top of this file.
Be careful when changing the values: the double quoting is intentional and needed. Don't remove.

Copyright ©2014 Juliette Reinders Folmer (Twitter: @jrf_nl / GitHub: @jrfnl)
License: DWTFYW
Updates will be published via: https://gist.github.com/jrfnl/3c28ea6d9b07fd48656d
Loosely inspired by: https://www.ruby-forum.com/topic/120869

NB: This was quickly thrown together. There are probably better ways to do bits of it.
Feel free to suggest them via the gist comment form ;-)
-->


<!--
The percentage of tests which need to have failed for the failures cell colour to go from orange to red.
Default value: 0.4 (=40%)
-->
<xsl:variable name="fail_bad" select="'0.4'" />

<!--
The thresholds used for the time colour coding in seconds.
-->
<xsl:variable name="very_bad_time" select="'10'" />
<xsl:variable name="bad_time" select="'3'" />
<xsl:variable name="not_so_good_time" select="'1'" />
<xsl:variable name="good_time" select="'0.4'" />


<!--
Whether to show the details of passed tests. Set to 1 to show.
Default value: 0 (no)
-->
<xsl:variable name="show_success_detail" select="'0'" />

<xsl:template match="/">
<html>
<head>
	<style type="text/css">
	* {
		font-family: tahoma, verdana, sans-serif;
		font-size: 96%;
	}
	a {
		text-decoration: none;
	}
	a:hover {
		text-decoration: underline;
	}
	h3, h4 {
		padding: 1em 0 0.5em;
	}
	table {
		width: 100%;
		border-collapse: collapse;
	}
	table#high-level {
		width: auto;
	}
	table tr {
		vertical-align: top;
	}
	table td, table th {
		padding: 0.2em 1em 0.3em 1em;
	}
	table th {
		text-align: left;
	}
	table tr.top {
		background-color: #dddddd;
		border-bottom: 2px solid #000000;
	}
	table tr.test-file {
		margin-top: 0.2em;
		font-weight: bold;
		background-color: #eeeeee;
		border-top: 1px solid #666666;
	}
	table tr.test-file th {
		padding-top: 0.5em;
	}
	table tr.single-test th {
		padding-left: 3em;
	}
	table#summary td {
		border-left: 1px solid #cccccc;
		border-right: 1px solid #cccccc;
	}
	table td.nr {
		text-align: center;
	}
	table td.time {
		text-align: right;
	}
	.pass {
		color: #999999;
		background-color: #C1FFC1;
	}
	.failed {
		background-color: #FFDAB9;
	}
	.errored {
		background-color: #FFB8BA;
	}
	.fail, .fail-lot {
		color: #FFFFEE;
		background-color: #C51F1F;
		font-weight: bold;
	}
	.fail-some {
		color: #FFFFEE;
		background-color: #FFB90F;
		font-weight: bold;
	}
	.very-bad-time {
		background-color: #FA8072;
		font-weight: bold;
	}
	.bad-time {
		background-color: #FFA54F;
	}
	.not-so-good-time {
		background-color: #FFEC8B;
	}
	.sort-of-ok {
		background-color: #BFEFFF;
	}
	.good-time {
		background-color: #C1FFC1;
	}

	.no-tests {
		text-decoration: line-through;
		text-decoration-style: double;
	}
	.error-detail-type, .fail-detail-type {
		padding-left: 2em;
		font-weight: bold;
	}
	.error-detail-detail, .fail-detail-detail {
		padding-left: 4em;
		white-space: pre-wrap;
		padding-bottom: 1em;
	}
	.backlink {
		text-align: right;
		font-size: 80%;
	}
	.backlink span {
		font-size: 140%;
	}
	</style>
</head>
<body>
	<xsl:apply-templates/>
</body>
</html>
</xsl:template>


<xsl:template match="/testsuites/testsuite" mode="high-level">
	<h2>Test Totals: <xsl:value-of select="@name"/></h2>
	<table id="high-level">
		<tr>
			<td><b>Number of Tests:</b></td>
			<td><xsl:value-of select="@tests"/></td>
		</tr>
		<tr>
			<td><b>Number of Assertions:</b></td>
			<td><xsl:value-of select="@assertions"/></td>
		</tr>
		<tr>
			<td><b>Number of Failures:</b></td>
			<td><xsl:value-of select="@failures"/></td>
		</tr>
		<tr>
			<td><b>Number of Errors:</b></td>
			<td><xsl:value-of select="@errors"/></td>
		</tr>
		<tr>
			<td><b>Execution Time:</b></td>
			<td><xsl:value-of select="@time"/></td>
		</tr>
	</table>
</xsl:template>


<xsl:template match="testsuites">
	<div>
    	<xsl:apply-templates select="/testsuites/testsuite" mode="high-level"/>
	</div>

	<h3>Summary</h3>
	<table id="summary">
		<tr class="top">
			<th>Name</th>
			<th>Tests</th>
			<th>Assertions</th>
			<th>Failures</th>
			<th>Errors</th>
			<th>Execution Time</th>
		</tr>
		<xsl:apply-templates select="//testsuite"/>
	</table>

	<xsl:apply-templates select="//testsuite[count(testsuite) = 0]" mode="details"/>

</xsl:template>


<xsl:template match="//testsuite">
	<xsl:variable name="hasfailures">
		<xsl:choose>
			<xsl:when test="(@failures div @tests ) > $fail_bad"> fail-lot</xsl:when>
			<xsl:when test="@failures &gt; 0"> fail-some</xsl:when>
			<xsl:when test="@tests &gt; 0 and @failures = 0 and @errors = 0"> pass</xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="haserrors">
		<xsl:choose>
			<xsl:when test="@errors &gt; 0"> fail</xsl:when>
			<xsl:when test="@tests &gt; 0 and @failures = 0 and @errors = 0"> pass</xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="passes">
		<xsl:choose>
			<xsl:when test="@tests &gt; 0 and @failures = 0 and @errors = 0"> pass</xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="isslow">
		<xsl:choose>
			<xsl:when test="@time &gt; $very_bad_time"> very-bad-time</xsl:when>
			<xsl:when test="@time &gt; $bad_time"> bad-time</xsl:when>
			<xsl:when test="@time &gt; $not_so_good_time"> not-so-good-time</xsl:when>
			<xsl:when test="@tests &gt; 0 and @time &lt; $good_time"> good-time</xsl:when>
			<xsl:when test="@tests = 0"></xsl:when>
			<xsl:otherwise> sort-of-ok</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="testname" select="@name" />

	<xsl:choose>
		<xsl:when test="count(testsuite) = 0">

		<tr class="single-test">
			<xsl:choose>
				<xsl:when test="count(testcase) &gt; 0">
			<th><a href="#{$testname}"><xsl:value-of select="@name"/></a></th>
				</xsl:when>
				<xsl:otherwise>
			<th><xsl:value-of select="@name"/></th>
				</xsl:otherwise>
			</xsl:choose>

			<td class="nr{$passes}"><xsl:value-of select="@tests"/></td>
			<td class="nr{$passes}"><xsl:value-of select="@assertions"/></td>
			<td class="nr{$hasfailures}"><xsl:value-of select="@failures"/></td>
			<td class="nr{$haserrors}"><xsl:value-of select="@errors"/></td>
			<td class="time{$isslow}"><xsl:value-of select="@time"/></td>
		</tr>

		</xsl:when>
		<xsl:when test="@name != ''">

		<tr class="test-file">
			<th><xsl:value-of select="@name"/></th>
			<td class="nr{$passes}"><xsl:value-of select="@tests"/></td>
			<td class="nr{$passes}"><xsl:value-of select="@assertions"/></td>
			<td class="nr{$hasfailures}"><xsl:value-of select="@failures"/></td>
			<td class="nr{$haserrors}"><xsl:value-of select="@errors"/></td>
			<td class="time{$isslow}"><xsl:value-of select="@time"/></td>
		</tr>
		</xsl:when>
		<xsl:otherwise></xsl:otherwise>
	</xsl:choose>
</xsl:template>


<xsl:template match="//testsuite[count(testsuite) = 0]" mode="details">
	<xsl:variable name="testname" select="@name" />

	<xsl:choose>
		<xsl:when test="count(testcase) &gt; 0 and ( $show_success_detail = 1 or ( $show_success_detail = 0 and ( @failures &gt; 0 or @errors &gt; 0 ) ) )">

	<h4 id="{$testname}"><xsl:value-of select="@name"/></h4>
	<table>
		<tr class="top">
			<th>Test name</th>
			<th>Assertions</th>
			<th>Time</th>
		</tr>
	    <xsl:apply-templates select="testcase"/>
	</table>
	<p class="backlink"><a href="#summary"><span>&#8613;</span> Back to summary</a></p>

		</xsl:when>

		<xsl:when test="count(testcase) = 0">
	<h4 id="{$testname}"><span class="no-tests"><xsl:value-of select="@name"/></span> (no tests found)</h4>
		</xsl:when>
		<xsl:otherwise></xsl:otherwise>

	</xsl:choose>
</xsl:template>

<xsl:template match="testcase">
	<xsl:variable name="isslow">
		<xsl:choose>
			<xsl:when test="@time &gt; $very_bad_time"> very-bad-time</xsl:when>
			<xsl:when test="@time &gt; $bad_time"> bad-time</xsl:when>
			<xsl:when test="@time &gt; $not_so_good_time"> not-so-good-time</xsl:when>
			<xsl:when test="@tests &gt; 0 and @time &lt; $good_time"> good-time</xsl:when>
			<xsl:when test="@tests = 0"></xsl:when>
			<xsl:otherwise> sort-of-ok</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

	<xsl:variable name="class">
		<xsl:choose>
			<xsl:when test="count(failure) &gt; 0">failed</xsl:when>
			<xsl:when test="count(error) &gt; 0">errored</xsl:when>
			<xsl:otherwise>pass</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>

		<tr class="{$class}">
			<th><xsl:value-of select="@name"/></th>
			<td><xsl:value-of select="@assertions"/></td>
			<td class="time{$isslow}"><xsl:value-of select="@time"/></td>
		</tr>

	<xsl:apply-templates select="error"/>
	<xsl:apply-templates select="failure"/>
</xsl:template>

<xsl:template match="error">
		<tr>
			<td class="error-detail-type" colspan="3"><xsl:value-of select="@type"/></td>
		</tr>
		<tr>
			<td class="error-detail-detail" colspan="3"><xsl:value-of select="."/></td>
		</tr>
</xsl:template>

<xsl:template match="failure">
		<tr>
			<td class="fail-detail-type" colspan="3"><xsl:value-of select="@type"/></td>
		</tr>
		<tr>
			<td class="fail-detail-detail" colspan="3"><xsl:value-of select="."/></td>
		</tr>
</xsl:template>

</xsl:stylesheet>