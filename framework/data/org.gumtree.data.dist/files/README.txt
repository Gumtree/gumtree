COMMON DATA MODEL API 2.1.0 (November 2011)
-------------------------------------------

1. Introduction
Common Data Model API (CDMA) is a library for loading and writing scientific
data from different data file format. It handles multi-dimentional array and
metadata in a hierarchical structure. CDMA consists of core API and a set of
implementations. To run CDMA, you need to have at least one implementation
available to the runtime.

2. Dependency

org.gumtree.data.core (API)
* com.springsource.org.jdom (optional)
* org.eclipse.osgi (optional)

org.gumtree.data.impl.netcdf (NetCDF based implementation)
* ncsa.hdf
* ucar.netcdf
* com.springsource.slf4j.api

org.gumtree.data.soleil (JNeXus based implementation)
* org.nexusformat
* fr.soleil.nexus4tango