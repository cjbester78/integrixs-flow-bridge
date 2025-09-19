package com.integrixs.soapbindings.infrastructure.wsdl;

import com.integrixs.soapbindings.domain.model.*;
import com.integrixs.soapbindings.domain.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * WSDL parser implementation
 */
@Component
public class WsdlParser {

    private static final Logger logger = LoggerFactory.getLogger(WsdlParser.class);

    private static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
    private static final String SOAP_NS = "http://schemas.xmlsoap.org/wsdl/soap/";
    private static final String SOAP12_NS = "http://schemas.xmlsoap.org/wsdl/soap12/";

    /**
     * Parse WSDL from content
     * @param wsdlContent WSDL content
     * @param location WSDL location
     * @return Parsed WSDL definition
     */
    public WsdlDefinition parse(String wsdlContent, String location) {
        logger.info("Parsing WSDL from location: {}", location);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(wsdlContent.getBytes(StandardCharsets.UTF_8)));

            Element root = document.getDocumentElement();

            WsdlDefinition wsdl = WsdlDefinition.builder()
                    .wsdlId(UUID.randomUUID().toString())
                    .name(root.getAttribute("name"))
                    .namespace(root.getAttribute("targetNamespace"))
                    .location(location)
                    .content(wsdlContent)
                    .type(determineWsdlType(document))
                    .services(new HashMap<>())
                    .namespaces(extractNamespaces(root))
                    .validated(true)
                    .build();

            // Parse services
            parseServices(document, wsdl);

            return wsdl;

        } catch(Exception e) {
            logger.error("Error parsing WSDL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse WSDL", e);
        }
    }

    private WsdlType determineWsdlType(Document document) {
        // Check for document/literal style
        NodeList bindings = document.getElementsByTagNameNS(WSDL_NS, "binding");
        for(int i = 0; i < bindings.getLength(); i++) {
            Element binding = (Element) bindings.item(i);
            NodeList soapBindings = binding.getElementsByTagNameNS(SOAP_NS, "binding");
            if(soapBindings.getLength() > 0) {
                Element soapBinding = (Element) soapBindings.item(0);
                String style = soapBinding.getAttribute("style");
                if("document".equalsIgnoreCase(style)) {
                    return WsdlType.IMPORTED;
                } else if("rpc".equalsIgnoreCase(style)) {
                    return WsdlType.GENERATED;
                }
            }
        }
        return WsdlType.IMPORTED; // Default
    }

    private Map<String, String> extractNamespaces(Element root) {
        Map<String, String> namespaces = new HashMap<>();
        org.w3c.dom.NamedNodeMap attributes = root.getAttributes();
        for(int i = 0; i < attributes.getLength(); i++) {
            org.w3c.dom.Node attr = attributes.item(i);
            if(attr.getNodeName().startsWith("xmlns")) {
                String prefix = attr.getNodeName().substring(5);
                if(prefix.startsWith(":")) {
                    prefix = prefix.substring(1);
                }
                namespaces.put(prefix.isEmpty() ? "default" : prefix, attr.getNodeValue());
            }
        }
        return namespaces;
    }

    private void parseServices(Document document, WsdlDefinition wsdl) {
        NodeList services = document.getElementsByTagNameNS(WSDL_NS, "service");

        for(int i = 0; i < services.getLength(); i++) {
            Element serviceElement = (Element) services.item(i);
            String serviceName = serviceElement.getAttribute("name");

            ServiceDefinition service = ServiceDefinition.builder()
                    .serviceName(serviceName)
                    .namespace(wsdl.getNamespace())
                    .ports(new ArrayList<>())
                    .build();

            // Parse ports
            NodeList ports = serviceElement.getElementsByTagNameNS(WSDL_NS, "port");
            for(int j = 0; j < ports.getLength(); j++) {
                Element portElement = (Element) ports.item(j);
                PortDefinition port = parsePort(document, portElement);
                service.getPorts().add(port);
            }

            wsdl.addService(service);
        }
    }

    private PortDefinition parsePort(Document document, Element portElement) {
        String portName = portElement.getAttribute("name");
        String bindingName = portElement.getAttribute("binding");

        // Get address from soap:address element
        String address = "";
        NodeList soapAddresses = portElement.getElementsByTagNameNS(SOAP_NS, "address");
        if(soapAddresses.getLength() > 0) {
            address = ((Element) soapAddresses.item(0)).getAttribute("location");
        }

        PortDefinition port = PortDefinition.builder()
                .portName(portName)
                .binding(bindingName)
                .address(address)
                .operations(new ArrayList<>())
                .build();

        // Parse operations from binding
        parseOperations(document, bindingName, port);

        return port;
    }

    private void parseOperations(Document document, String bindingName, PortDefinition port) {
        // Find binding element
        NodeList bindings = document.getElementsByTagNameNS(WSDL_NS, "binding");
        for(int i = 0; i < bindings.getLength(); i++) {
            Element binding = (Element) bindings.item(i);
            if(bindingName.contains(binding.getAttribute("name"))) {
                // Parse operations
                NodeList operations = binding.getElementsByTagNameNS(WSDL_NS, "operation");
                for(int j = 0; j < operations.getLength(); j++) {
                    Element operationElement = (Element) operations.item(j);
                    OperationDefinition operation = parseOperation(operationElement);
                    port.getOperations().add(operation);
                }
                break;
            }
        }
    }

    private OperationDefinition parseOperation(Element operationElement) {
        String operationName = operationElement.getAttribute("name");

        // Get SOAP action
        String soapAction = "";
        NodeList soapOperations = operationElement.getElementsByTagNameNS(SOAP_NS, "operation");
        if(soapOperations.getLength() > 0) {
            soapAction = ((Element) soapOperations.item(0)).getAttribute("soapAction");
        }

        return OperationDefinition.builder()
                .operationName(operationName)
                .soapAction(soapAction)
                .documentation(null)
                .build();
    }

    private String extractDocumentation(Element element) {
        NodeList docs = element.getElementsByTagNameNS(WSDL_NS, "documentation");
        if(docs.getLength() > 0) {
            return docs.item(0).getTextContent().trim();
        }
        return null;
    }
}
