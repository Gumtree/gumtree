package fr.soleil.nexus;



public class PathGroup extends PathNexus {

    /**
     * PathGroup
     * Create an object PathGroup
     *
     * @param sGroups array containing name of each group.
     * @note group's class can be specified by adding "<" and ">" to a class name: i.e. "my_entry<NXentry>"
     * @note be aware that it's better not to force the group class by default they are mapped by the API
     */

    public    PathGroup(String[] sGroups)    { super(sGroups); }
    public    PathGroup(PathNexus pnPath)    { super(pnPath.getGroups()); this.setFile(pnPath.getFilePath()); }
    protected  PathGroup(String sAcquiName)  { super(new String[] {sAcquiName}); }


    static public PathGroup Convert(PathNexus pnPath)
    {
	return new PathGroup(pnPath);
    }

}

