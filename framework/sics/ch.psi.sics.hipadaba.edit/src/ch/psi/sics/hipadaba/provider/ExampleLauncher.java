package ch.psi.sics.hipadaba.provider;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.compare.diff.metamodel.ComparisonResourceSnapshot;
import org.eclipse.emf.compare.diff.metamodel.DiffFactory;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.eclipse.emf.compare.diff.service.DiffService;
import org.eclipse.emf.compare.match.MatchOptions;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.compare.util.ModelUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import ch.psi.sics.hipadaba.impl.HipadabaPackageImpl;

/**
 * This application will try and launch an headless model comparison.
 * 
 * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
 */
public final class ExampleLauncher {
	/**
	 * This class doesn't need to be instantiated.
	 */
	private ExampleLauncher() {
		// prevents instantiation
	}

	/**
	 * Launcher of this application.
	 * 
	 * @param args
	 *            Arguments of the launch.
	 */
	public static void main(String[] args) {
		if (args.length == 2 && new File(args[0]).canRead() && new File(args[1]).canRead()) {
//		if (true) {
			// Creates the resourceSets where we'll load the models
			final ResourceSet resourceSet1 = new ResourceSetImpl();
			resourceSet1.getPackageRegistry().put(HipadabaPackageImpl.eNS_URI,
					HipadabaPackageImpl.eINSTANCE);
			final ResourceSet resourceSet2 = new ResourceSetImpl();
			resourceSet2.getPackageRegistry().put(HipadabaPackageImpl.eNS_URI,
					HipadabaPackageImpl.eINSTANCE);
			// Register additionnal packages here. For UML2 for instance :
			// Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,
			// UMLResource.Factory.INSTANCE);
			// resourceSet1.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
			// resourceSet2.getPackageRegistry().put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);

			try {
				System.out.println("Loading resources.\n"); //$NON-NLS-1$
				// Loads the two models passed as arguments
				final EObject model1 = ModelUtils.load(new File(args[0]), resourceSet1);
				final EObject model2 = ModelUtils.load(new File(args[1]), resourceSet2);
				
//				final EObject model1 = ShipadabaFactoryImpl.eINSTANCE.createDocumentRoot().getSICS();
//				final EObject model2 = ShipadabaFactoryImpl.eINSTANCE.createDocumentRoot().getSICS();
				
				// Creates the match then the diff model for those two models
				System.out.println("Matching models.\n"); //$NON-NLS-1$
				Map<String, Object> options = new HashMap<String, Object>();
//				options.put(MatchOptions.OPTION_DISTINCT_METAMODELS, false);
				options.put(MatchOptions.OPTION_SEARCH_WINDOW, 1);

				final MatchModel match = MatchService.doMatch(model1, model2, options);
				
				System.out.println("Differencing models.\n"); //$NON-NLS-1$
				final DiffModel diff = DiffService.doDiff(match);

//				System.out.println("Merging difference to args[1].\n"); //$NON-NLS-1$
//				final List<DiffElement> differences = new ArrayList<DiffElement>(diff.getOwnedElements());
//				// This will merge all references to the right model (second argument).
//				MergeService.merge(differences, true);

				// Prints the results
				try {
					System.out.println("MatchModel :\n"); //$NON-NLS-1$
//					System.out.println(ModelUtils.serialize(match));
					System.out.println("DiffModel :\n"); //$NON-NLS-1$
					System.out.println(ModelUtils.serialize(diff));
				} catch (final IOException e) {
					e.printStackTrace();
				}

				// Serializes the result as "result.emfdiff" in the directory this class has been called from.
				System.out.println("saving emfdiff as \"result.emfdiff\""); //$NON-NLS-1$
				final ComparisonResourceSnapshot snapshot = DiffFactory.eINSTANCE
						.createComparisonResourceSnapshot();
				snapshot.setDate(Calendar.getInstance().getTime());
//				snapshot.setMatch(match);
				snapshot.setDiff(diff);
				ModelUtils.save(snapshot, "result.emfdiff"); //$NON-NLS-1$
			} catch (final IOException e) {
				// shouldn't be thrown
				e.printStackTrace();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("usage : ExampleLauncher <Model1> <Model2>"); //$NON-NLS-1$
		}
	}
}