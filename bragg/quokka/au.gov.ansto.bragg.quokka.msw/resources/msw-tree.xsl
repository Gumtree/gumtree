<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
  xmlns:msw="http://www.gumtree.org/msw"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">
  
  <xsl:template match="/msw:MSW">
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="msw.css"/>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="msw:LoopHierarchy">
    <xsl:call-template name="msw:Tree">
      <xsl:with-param name="index" select="count(*)"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="msw:Tree">
    <xsl:param name="index"/>
    <xsl:if test="$index > 0">
      <xsl:apply-templates select="/msw:MSW/msw:LoopHierarchy/*[$index]">
        <xsl:with-param name="index" select="$index"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

  <xsl:template name="msw:SubTree">
    <xsl:param name="index"/>
    <xsl:if test="$index > 1">
      <tr>
        <td class="tree" colspan="99">
          <xsl:call-template name="msw:Tree">
            <xsl:with-param name="index" select="$index - 1"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="msw:SampleList">
    <xsl:param name="index"/>
    <p>SampleList (<xsl:value-of select="@Id"/>)</p>
    <table>
      <tr>
        <th>Id</th>
        <th>Name</th>
        <th>Thickness</th>
        <th>Description</th>
      </tr>
      <xsl:for-each select="msw:Sample">
        <tr>
          <td><xsl:value-of select="@Id"/></td>
          <td><xsl:value-of select="@Name"/></td>
          <td><xsl:value-of select="@Thickness"/></td>
          <td><xsl:value-of select="@Description"/></td>
        </tr>
        <xsl:call-template name="msw:SubTree">
          <xsl:with-param name="index" select="$index"/>
        </xsl:call-template>
      </xsl:for-each>
    </table>
  </xsl:template>
  
  <xsl:template match="msw:ConfigurationList">
    <xsl:param name="index"/>
    <p>ConfigurationList (<xsl:value-of select="@Id"/>)</p>
    <table>
      <tr>
        <th>Id</th>
        <th>Name</th>
        <th>Description</th>
        <th>SetupScript</th>
      </tr>
      <xsl:for-each select="msw:Configuration">
        <tr>
          <td><xsl:value-of select="@Id"/></td>
          <td><xsl:value-of select="@Name"/></td>
          <td><xsl:value-of select="@Description"/></td>
          <td><xsl:value-of select="msw:SetupScript"/></td>
        </tr>
        <tr>
          <td colspan="4">
            <xsl:call-template name="msw:MeasurementList">
              <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>
  
  <xsl:template name="msw:MeasurementList">
    <xsl:param name="index"/>
    <table>
      <tr>
        <th>Id</th>
        <th>Name</th>
        <th>MinTime</th>
        <th>MaxTime</th>
        <th>MonitorCounts</th>
        <th>DetectorCounts</th>
        <th>SetupScript</th>
      </tr>
      <xsl:for-each select="msw:Measurement">
        <tr>
          <td><xsl:value-of select="@Id"/></td>
          <td><xsl:value-of select="@Name"/></td>
          <td><xsl:value-of select="@MinTime"/></td>
          <td><xsl:value-of select="@MaxTime"/></td>
          <td><xsl:value-of select="@MonitorCounts"/></td>
          <td><xsl:value-of select="@DetectorCounts"/></td>
          <td><xsl:value-of select="msw:SetupScript"/></td>
        </tr>
        <xsl:call-template name="msw:SubTree">
          <xsl:with-param name="index" select="$index"/>
        </xsl:call-template>
      </xsl:for-each>
    </table>
  </xsl:template>
  
  <xsl:template match="msw:Environment">
    <xsl:param name="index"/>
    <p><xsl:value-of select="@Name"/> (<xsl:value-of select="@Id"/>)</p>
    <table>
      <tr>
        <th>Id</th>
        <th>Value</th>
        <th>WaitPeriod</th>
      </tr>
      <xsl:for-each select="msw:SetPoint">
        <tr>
          <td><xsl:value-of select="@Id"/></td>
          <td><xsl:value-of select="@Value"/></td>
          <td><xsl:value-of select="@WaitPeriod"/></td>
        </tr>
        <xsl:call-template name="msw:SubTree">
          <xsl:with-param name="index" select="$index"/>
        </xsl:call-template>
      </xsl:for-each>
    </table>
  </xsl:template>
  
</xsl:stylesheet>