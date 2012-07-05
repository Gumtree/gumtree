/**
 * @brief The CDMA is an API that provides an access to data sources regardless their format or structure.
 * 
 * The CDMA abstracts the data source constraints of format and structure. It provides a simple way of browsing and loading data using a set of interfaces. Therefore application developers shouldn't care of the data source that their programs uses and focus only on their application process.
 * <p>
 * The CDMA Core is an API that accesses data through a data format plug-in mechanism and scientific applications definitions (sets of keywords) coming from a consensus between scientists and institutes.
 * To do so, it will manages plug-in's of different format (EDF, NetCDF, NeXus, SQL ...) from various institutes.
 * <p>
 * The CDMA Core offers:
 * <br/> - a plug-in auto detection mechanism to not care of which plug-in a data source should refer to (see: the Factory class)
 * <br/> - a set of interfaces, that represent the common part of all plug-in, permitting to browse and load data from a source (see: package interfaces)
 * <br/> - an Extended Dictionary mechanism to abstract the physical structure of a data source (see: package dictionary)
 */
package org.gumtree.data;