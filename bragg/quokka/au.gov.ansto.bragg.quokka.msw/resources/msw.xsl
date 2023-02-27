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

  <xsl:template match="msw:UserList">
    <p>UserList (<xsl:value-of select="@Id"/>)</p>
    <table>
      <tr>
        <th>Id</th>
        <th>Name</th>
        <th>Phone</th>
        <th>Email</th>
      </tr>
      <xsl:for-each select="msw:User">
        <tr>
          <td><xsl:value-of select="@Id"/></td>
          <td><xsl:value-of select="@Name"/></td>
          <td><xsl:value-of select="@Phone"/></td>
          <td><xsl:value-of select="@Email"/></td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="msw:LoopHierarchy">
    <xsl:apply-templates>
      <xsl:sort select="position()" data-type="number" order="descending"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="msw:SampleList">
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
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="msw:ConfigurationList">
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
          <td class="measurements" colspan="4">
            <xsl:call-template name="msw:MeasurementList"/>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template name="msw:MeasurementList">
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
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="msw:Environment">
    <p><xsl:value-of select="@Name"/> (<xsl:value-of select="@Id"/>)</p>
    <table>
      <tr>
        <th>Id</th>
        <th>Value</th>
        <th>WaitPeriod</th>
        <th>TimeEstimate</th>
      </tr>
      <xsl:for-each select="msw:SetPoint">
        <tr>
          <td><xsl:value-of select="@Id"/></td>
          <td><xsl:value-of select="@Value"/></td>
          <td><xsl:value-of select="@WaitPeriod"/></td>
          <td><xsl:value-of select="@TimeEstimate"/></td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>
  
</xsl:stylesheet>