package org.gumtree.data.soleil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.gumtree.data.IDatasource;
import org.gumtree.data.exception.NoResultException;
import org.gumtree.data.interfaces.IDataset;
import org.gumtree.data.interfaces.IGroup;
import org.gumtree.data.soleil.navigation.NxsDataset;
import org.nexusformat.NexusException;

import fr.soleil.nexus.DataItem;
import fr.soleil.nexus.NexusFileReader;
import fr.soleil.nexus.NexusNode;
import fr.soleil.nexus.PathGroup;
import fr.soleil.nexus.PathNexus;

public final class NxsDatasource implements IDatasource {
    private static final int EXTENSION  = 4;
    private static final String CREATOR = "Synchrotron SOLEIL";
    private static final String[] BEAMLINES = new String[] {"CONTACQ", "AILES", "ANTARES", "CASSIOPEE", "CRISTAL", "DIFFABS", "DEIMOS", "DESIRS", "DISCO", "GALAXIES", "LUCIA", "MARS", "METROLOGIE", "NANOSCOPIUM", "ODE", "PLEIADES", "PROXIMA1", "PROXIMA2", "PSICHE", "SAMBA", "SEXTANTS", "SIRIUS", "SIXS", "SMIS", "TEMPO", "SWING"};

    public static final class NeXusFilter implements FilenameFilter {
        public static final int    EXTENSION_LENGTH = 4;
        public static final String EXTENSION = ".nxs";

        public boolean accept(File dir, String name) {
            return (name.endsWith(NeXusFilter.EXTENSION));
        }
    }

    @Override
    public String getFactoryName() {
        return NxsFactory.NAME;
    }

    @Override
    public boolean isReadable(URI target) {
        File file      = new File(target);
        String name    = file.getName();
        int length     = name.length();
        boolean result = false;

        if( file.isDirectory() ) {
            // Check if the folder is an aggregated dataset  
            if( isDatasetFolder(file) ) {
                result = true;
            }
        }
        // Check if the URI is a NeXus file
        else if( length > EXTENSION && name.substring(length - EXTENSION).equals(".nxs") ) {
            result = true;
        }
        return result;
    }

    @Override
    public boolean isProducer(URI target) {
        boolean result = false;
        //if( config != null ) {
        if( isReadable(target) ) {
            File file = new File(target);
            IDataset dataset = null;
            try {
                // instantiate
                dataset = NxsDataset.instanciate( file );

                // open file
                dataset.open();

                // seek at root for 'creator' attribute
                IGroup group = dataset.getRootGroup();
                if( group.hasAttribute("creator", CREATOR) ) {
                    result = true;
                }
                else {
                    group = group.getGroup("<NXentry>");
                    if( group != null ) {
                        group = group.getGroup("<NXinstrument>");
                    }

                    if( group != null ) {
                        String node = group.getShortName();

                        for( String name : BEAMLINES ) {
                            if( node.equalsIgnoreCase(name) ) {
                                result = true;
                                break;
                            }
                        }
                    }
                }

                // close file
                dataset.close();

            } catch (IOException e) {
                // close file
                if( dataset != null ) {
                    try {
                        dataset.close();
                    } catch (IOException e1) {
                    }
                }
            } catch (NoResultException e) {
                e.printStackTrace();
            }
        }
        //}
        return result;
    }

    @Override
    public boolean isExperiment(URI target) {
        File file      = new File(target);
        String name    = file.getName();
        int length     = name.length();
        boolean result = false;

        if( file.isDirectory() ) {
            // Check if the folder is an aggregated dataset  
            if( isDatasetFolder(file) ) {
                result = true;
            }
        }
        // Check if the URI is a NeXus file
        else if( name.substring(length - EXTENSION).equals(".nxs") ) {
            result = true;
        }

        return result;
    }

    @Override
    public boolean isBrowsable(URI target) {
        return isReadable(target);
    }

    // ---------------------------------------------------------
    /// private methods
    // ---------------------------------------------------------
    private static boolean isDatasetFolder(File file) {
        boolean result = false;
        if( file.isDirectory() ) {
            NeXusFilter filter = new NeXusFilter();
            for( File nxFile : file.listFiles(filter) ) {
                NexusFileReader reader = new NexusFileReader(nxFile.getAbsolutePath());
                PathNexus path = new PathGroup(new String[] {"<NXentry>", "<NXdata>"} );
                try {
                    reader.openFile();
                    reader.openPath(path);
                    ArrayList<NexusNode> list = reader.listChildren();
                    for( NexusNode node : list ) {
                        reader.openNode(node);
                        DataItem data = reader.readDataInfo();
                        if( data.getAttribute("dataset_part") != null ) {
                            result = true;
                            reader.closeFile();
                            break;
                        }
                        reader.closeData();

                    }
                    reader.closeFile();
                } catch (NexusException e1) {
                    try {
                        reader.closeFile();
                    }
                    catch (NexusException e2) {} 
                    finally {
                        result = false;
                    }
                }
                if( result ) {
                    break;
                }
            }
        }
        return result;
    }
}
