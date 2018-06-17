<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:mvn="http://maven.apache.org/POM/4.0.0"
                xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

  <xsl:template match="mvn:build">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>


      </xsl:copy>
  </xsl:template>

  <xsl:template match="mvn:version[../mvn:groupId='de.epost.billing.tools.buildchaingenerator' and
                                   ../mvn:artifactId='buildchaingenerator']">
    <xsl:copy>
      <xsl:text>d</xsl:text>
      <!--
      <xsl:apply-templates select="@*|node()"/>
      -->

    </xsl:copy>
  </xsl:template>


  <xsl:template match="@*|node()|comment()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>