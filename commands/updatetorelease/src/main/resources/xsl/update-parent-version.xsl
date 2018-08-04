<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:mvn="http://maven.apache.org/POM/4.0.0"
                xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

  <xsl:param name="version_information" />
  <xsl:param name="group_id" />
  <xsl:param name="artifact_id" />


  <xsl:template match="mvn:version[parent::mvn:parent and
                       ../mvn:groupId/text() = $group_id and
                       ../mvn:artifactId/text() = $artifact_id]">
    <xsl:copy>
      <xsl:copy-of select="$version_information" />
    </xsl:copy>
    <!--xsl:message terminate="no">Juhu, gefunden!</xsl:message-->
  </xsl:template>

  <xsl:template match="@*|node()|comment()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>


  </xsl:template>

</xsl:stylesheet>
