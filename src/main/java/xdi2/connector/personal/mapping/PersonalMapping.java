package xdi2.connector.personal.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.dictionary.Dictionary;
import xdi2.core.features.equivalence.Equivalence;
import xdi2.core.features.nodetypes.XdiAbstractContext;
import xdi2.core.features.nodetypes.XdiAttributeSingleton;
import xdi2.core.features.nodetypes.XdiEntitySingleton;
import xdi2.core.impl.memory.MemoryGraphFactory;
import xdi2.core.io.XDIReaderRegistry;
import xdi2.core.xri3.XDI3Segment;
import xdi2.core.xri3.XDI3SubSegment;

public class PersonalMapping {

	public static final XDI3SubSegment XRI_S_PERSONAL_CONTEXT = XDI3SubSegment.create("+(https://personal.com/)");

	private static final Logger log = LoggerFactory.getLogger(PersonalMapping.class);

	private static PersonalMapping instance;

	private Graph mappingGraph;

	public PersonalMapping() {

		this.mappingGraph = MemoryGraphFactory.getInstance().openGraph();

		try {

			XDIReaderRegistry.getAuto().read(this.mappingGraph, PersonalMapping.class.getResourceAsStream("mapping.xdi"));
		} catch (Exception ex) {

			throw new Xdi2RuntimeException(ex.getMessage(), ex);
		}
	}

	public static PersonalMapping getInstance() {

		if (instance == null) instance = new PersonalMapping();

		return instance;
	}

	/**
	 * Converts a Personal data XRI to a native Personal gem identifier.
	 * Example: +(0000)$!(+(preferred_first_name)) --> 0000
	 */
	public String personalDataXriToPersonalGemIdentifier(XDI3Segment personalDataXri) {

		if (personalDataXri == null) throw new NullPointerException();

		// convert

		String personalGemIdentifier = Dictionary.instanceXriToNativeIdentifier(XdiAbstractContext.getBaseArcXri(personalDataXri.getSubSegment(0)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + personalDataXri + " to " + personalGemIdentifier);

		return personalGemIdentifier;
	}

	/**
	 * Converts a Personal data XRI to a native Personal field identifier.
	 * Example: +(0000)$!(+(preferred_first_name)) --> preferred_first_name
	 */
	public String personalDataXriToPersonalFieldIdentifier(XDI3Segment personalDataXri) {

		if (personalDataXri == null) throw new NullPointerException();

		// convert

		String personalFieldIdentifier = Dictionary.instanceXriToNativeIdentifier(XdiAbstractContext.getBaseArcXri(personalDataXri.getSubSegment(1)));

		// done

		if (log.isDebugEnabled()) log.debug("Converted " + personalDataXri + " to " + personalFieldIdentifier);

		return personalFieldIdentifier;
	}

	/**
	 * Maps and converts a Personal data XRI to an XDI data XRI.
	 * Example: +(0000)$!(+(preferred_first_name)) --> +first$!(+name)
	 */
	public XDI3Segment personalDataXriToXdiDataXri(XDI3Segment personalDataXri) {

		if (personalDataXri == null) throw new NullPointerException();

		// convert
		
		StringBuffer buffer1 = new StringBuffer();

		for (int i=0; i<personalDataXri.getNumSubSegments(); i++) {
			
			buffer1.append(Dictionary.instanceXriToDictionaryXri(XdiAbstractContext.getBaseArcXri(personalDataXri.getSubSegment(i))));
		}

		// map

		XDI3Segment personalDataDictionaryXri = XDI3Segment.create("" + XRI_S_PERSONAL_CONTEXT + buffer1.toString());
		ContextNode personalDataDictionaryContextNode = this.mappingGraph.findContextNode(personalDataDictionaryXri, false);
		if (personalDataDictionaryContextNode == null) return null;

		ContextNode xdiDataDictionaryContextNode = Equivalence.getReferenceContextNode(personalDataDictionaryContextNode);
		XDI3Segment xdiDataDictionaryXri = xdiDataDictionaryContextNode.getXri();

		// convert

		StringBuilder buffer2 = new StringBuilder();

		for (int i=0; i<xdiDataDictionaryXri.getNumSubSegments(); i++) {

			if (i + 1 < xdiDataDictionaryXri.getNumSubSegments()) {

				buffer2.append(XdiEntitySingleton.createArcXri(Dictionary.dictionaryXriToInstanceXri(xdiDataDictionaryXri.getSubSegment(i))));
			} else {

				buffer2.append(XdiAttributeSingleton.createArcXri(Dictionary.dictionaryXriToInstanceXri(xdiDataDictionaryXri.getSubSegment(i))));
			}
		}

		XDI3Segment xdiDataXri = XDI3Segment.create(buffer2.toString());

		// done

		if (log.isDebugEnabled()) log.debug("Mapped and converted " + personalDataXri + " to " + xdiDataXri);

		return xdiDataXri;
	}

	/**
	 * Maps and converts an XDI data XRI to a Personal data XRI.
	 * Example: +first$!(+name) --> +(0000)$!(+(preferred_first_name)) 
	 */
	public XDI3Segment xdiDataXriToPersonalDataXri(XDI3Segment xdiDataXri) {

		if (xdiDataXri == null) throw new NullPointerException();

		// convert
		
		StringBuffer buffer1 = new StringBuffer();

		for (int i=0; i<xdiDataXri.getNumSubSegments(); i++) {
			
			buffer1.append(Dictionary.instanceXriToDictionaryXri(XdiAbstractContext.getBaseArcXri(xdiDataXri.getSubSegment(i))));
		}

		// map
		
		XDI3Segment xdiDataDictionaryXri = XDI3Segment.create(buffer1.toString());
		ContextNode xdiDataDictionaryContextNode = this.mappingGraph.findContextNode(xdiDataDictionaryXri, false);
		if (xdiDataDictionaryContextNode == null) return null;

		ContextNode personalDataDictionaryContextNode = Equivalence.getIncomingReferenceContextNodes(xdiDataDictionaryContextNode).next();
		XDI3Segment personalDataDictionaryXri = personalDataDictionaryContextNode.getXri();
		
		// convert

		StringBuilder buffer2 = new StringBuilder();

		for (int i=1; i<personalDataDictionaryXri.getNumSubSegments(); i++) {

			if (i + 1 < personalDataDictionaryXri.getNumSubSegments()) {

				buffer2.append(XdiEntitySingleton.createArcXri(Dictionary.dictionaryXriToInstanceXri(personalDataDictionaryXri.getSubSegment(i))));
			} else {

				buffer2.append(XdiAttributeSingleton.createArcXri(Dictionary.dictionaryXriToInstanceXri(personalDataDictionaryXri.getSubSegment(i))));
			}
		}

		XDI3Segment personalDataXri = XDI3Segment.create(buffer2.toString());

		// done

		if (log.isDebugEnabled()) log.debug("Mapped and converted " + xdiDataXri + " to " + personalDataXri);

		return personalDataXri;
	}

	/*
	 * Getters and setters
	 */

	public Graph getMappingGraph() {

		return this.mappingGraph;
	}

	public void setMappingGraph(Graph mappingGraph) {

		this.mappingGraph = mappingGraph;
	}
}
