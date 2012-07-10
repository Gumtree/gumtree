package org.gumtree.data.soleil.dictionary;

import java.util.regex.Pattern;

import org.gumtree.data.Factory;
import org.gumtree.data.IFactory;
import org.gumtree.data.dictionary.IPathParamResolver;
import org.gumtree.data.dictionary.IPathParameter;
import org.gumtree.data.dictionary.impl.Path;
import org.gumtree.data.dictionary.impl.PathParameter;
import org.gumtree.data.interfaces.IContainer;
import org.gumtree.data.soleil.NxsFactory;
import org.gumtree.data.utils.Utilities.ParameterType;

public final class NxsPathParamResolver implements IPathParamResolver {
    private Path   mPath;
    private String mPathSeparator;

    public NxsPathParamResolver(IFactory factory, Path path) {
        mPathSeparator = factory.getPathSeparator();
        mPath = path;
    }


    @Override
    public IPathParameter resolvePathParameter(IContainer item) {
        IPathParameter result = null;
        String[] groupParts = item.getName().split( Pattern.quote(mPathSeparator) );
        String[] pathParts  = mPath.getValue().split( Pattern.quote(mPathSeparator) );
        String part, param, buff;

        // Parse the path
        for( int depth = 0; depth < groupParts.length; depth++ ) {
            if( depth < pathParts.length ) {
                part  = groupParts[depth];
                param = pathParts[depth];

                // If a parameter is defined
                if( param.matches( ".*(" + mPath.PARAM_PATTERN  + ")+.*") ) {
                    // Try to determine the parameter result
                    buff = param.replaceFirst("^(.*)(" + mPath.PARAM_PATTERN + ".*)$", "$1");
                    part = part.substring(buff.length());
                    buff = param.replaceAll(".*" + mPath.PARAM_PATTERN, "");
                    part = part.replaceFirst(Pattern.quote(buff), "");
                    buff = param.replaceAll(".*" + mPath.PARAM_PATTERN + ".*", "$1");
                    result = new PathParameter(Factory.getFactory(getFactoryName()), ParameterType.SUBSTITUTION, buff, part);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String getFactoryName() {
        return NxsFactory.NAME;
    }

}
