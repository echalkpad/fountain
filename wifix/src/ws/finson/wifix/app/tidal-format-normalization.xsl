<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">
    <xsl:output method="xml" indent="yes"/>
    
    <xsl:template match="/session">
        <session>
            <xsl:apply-templates select="context"/>
            <table>
                <xsl:apply-templates select="datainfo"/>
            </table>
        </session>
    </xsl:template>
    
    <xsl:template match="context">
        <xsl:copy-of select="."/>
    </xsl:template>
    
    <xsl:template match="datainfo">
        <col name="{stationname}" >
                 <xsl:apply-templates select="data/item"/>               
        </col>
    </xsl:template>
    
    <xsl:template match="item">
        <value><xsl:value-of select="pred"/></value>
    </xsl:template>
    
</xsl:stylesheet>