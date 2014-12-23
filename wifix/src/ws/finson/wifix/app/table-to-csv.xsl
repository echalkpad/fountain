<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text" />
    <!-- transform selected data from an XML table layout to a text-nodes-only csv format -->
    
    <xsl:variable name="rowCount"><xsl:value-of select="count(//column[1]/value)" /></xsl:variable>

    <xsl:template match="//table">
        <xsl:apply-templates select="column" />
        <xsl:for-each select="column[1]/value">
          <xsl:apply-templates select="." mode="row-index">
            <xsl:with-param name="row"><xsl:value-of select="position()" /></xsl:with-param>
         </xsl:apply-templates>
        </xsl:for-each>
     </xsl:template>
     
     <xsl:template match="column">"<xsl:value-of select="./@label" />"<xsl:choose><xsl:when test="position()!=last()">,</xsl:when><xsl:when test="position()=last()" ><xsl:text>&#xa;</xsl:text></xsl:when></xsl:choose></xsl:template>
     
     <xsl:template match="value" mode="row-index">
     <xsl:param name="row">1</xsl:param>
        <xsl:apply-templates select="//column/value[$row]" mode="data-output" />
     </xsl:template>
    
     <xsl:template match="value" mode="data-output">
     <xsl:value-of select="." /><xsl:choose><xsl:when test="position()!=last()">,</xsl:when><xsl:when test="position()=last()" ><xsl:text>&#xa;</xsl:text></xsl:when></xsl:choose></xsl:template>
     
</xsl:stylesheet>