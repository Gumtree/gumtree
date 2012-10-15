/**
 * @brief The CDMA interfaces package contains all interfaces a plug-in must implement.
 * 
 * All interfaces, but the IContainer one, in this package should be implemented by a plug-in.
 * To be functional a plug-in should also implement the IFactory interface in the package
 * org.gumtree.data so it can create CDMA objects.
 * 
 * To activate the Extended Dictionary the interface IPathParamResolver should be implemented.
 * Indeed it aims to resolve the paths while using that mechanism.
 */

package org.gumtree.data.interfaces;