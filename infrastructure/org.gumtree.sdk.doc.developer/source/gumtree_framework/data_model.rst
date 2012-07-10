Common Data Model
*****************

Data structure is probably one of the most important area of software design. 
It is even more important for scientific software where application has to acquire 
and process large amount of numerical data. Those data can have different type (
integer or floating point) and can be stored in multiple dimensions. Gumtree provide
a convenience way to handle data by using the common data model API. This API 
provides the following features:

* Support reading and saving data from various data formats (HDF, ASCII, XML, etc)
* Hierarchical structure for holding multiple data objects in a single container
* Allow data to be stored in multi dimensional array
