<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">
    <xsl:output method="xml" indent="yes"/>
    
<!-- transform tree from a parameter layout to a table layout -->    
    
    <xsl:template match="/session">
    <dataset>
      <table-sequence>
        <table>
           <xsl:apply-templates select="parameter-sequence/parameter"/>
        </table>
      </table-sequence>
    </dataset>
    </xsl:template>
    
    <xsl:template match="parameter">
        <column label="{@name}" >
            <xsl:apply-templates select="parameter-values/value"/>               
        </column>
    </xsl:template>
    
    <xsl:template match="value">
        <value><xsl:value-of select="."/></value>
    </xsl:template>
    
</xsl:stylesheet>