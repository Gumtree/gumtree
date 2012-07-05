/**
 * @brief The CDMA dictionary package describes the Extended Dictionary mechanism.
 *
 * The Extended Dictionary mechanism permits to abstract the data source physical structure.
 * It proposes to not rely on the physical structure of a data source, but to use a virtual 
 * structure that the plug-in will conform to.
 * <p>
 * A physical browsing require to know where and how data are stored according a particular
 * institute's format. At the opposite the Extended Dictionary mechanism defines how data is
 * expected by the above application according an experiment. The dictionary associate keys
 * (that have a physical meaning to a path in the data source: IKey) to a path (defined 
 * by a plug-in mapping dictionary: IPath). Thus the plug-in should only care of resolving 
 * the path to return the requested data. The API user only cares about data manipulation 
 * not any more on its access.
 * <p>
 * Main objects that allow such an use are IKey, ILogicalGroup and IExtendedDictionary. The
 * ILogicalGroup contains a IExtendedDictionary which have all definitions and there logical
 * organization. The group gives access to several sub-group and and IDataItem.
 * <p>
 * A default implementation of all those interfaces is available in the packages 'org.gumtree.data.dictionary.impl'.
 * The plug-in must only implements the IPathParamResolver interface to activate the Extended Dictionary mechanism.
 */
package org.gumtree.data.dictionary;