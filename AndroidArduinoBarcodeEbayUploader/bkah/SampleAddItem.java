
/**
 * This program is licensed under the terms of the eBay Common Development and
 * Distribution License (CDDL) Version 1.0 (the "License") and any subsequent
 * version thereof released by eBay.  The then-current version of the License
 * can be found at http://www.opensource.org/licenses/cddl1.php
 */

package com.ebay.api.soap.examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Properties;

import javax.xml.rpc.ServiceException;


import com.ebay.api.soap.AddItemRequestType;
import com.ebay.api.soap.AddItemResponseType;
import com.ebay.api.soap.AmountType;
import com.ebay.api.soap.BuyerPaymentMethodCodeType;
import com.ebay.api.soap.CategoryType;
import com.ebay.api.soap.CountryCodeType;
import com.ebay.api.soap.CurrencyCodeType;
import com.ebay.api.soap.DetailLevelCodeType;
import com.ebay.api.soap.EBayAPIInterface;
import com.ebay.api.soap.EBayAPIInterfaceService;
import com.ebay.api.soap.EBayAPIInterfaceServiceLocator;
import com.ebay.api.soap.EBayAPISoapBindingStub;
import com.ebay.api.soap.ErrorType;
import com.ebay.api.soap.FeeType;
import com.ebay.api.soap.ItemType;
import com.ebay.api.soap.ListingDurationCodeType;
import com.ebay.api.soap.ShippingDetailsType;
import com.ebay.api.soap.ShippingTypeCodeType;
import com.ebay.api.soap.ShippingServiceOptionsType;
import com.ebay.api.soap.AmountType;
/**
 * @author JOHNSONAA
 *
 */

public class SampleAddItem {


	public static void main(String[] args) {

		String properties = "config.properties";
		if (args.length > 0) {
			properties = args[0];
		}

		Properties keys = new Properties();
	    try {
	        keys.load(new FileInputStream(properties));
	    } catch (IOException e) {
	        System.out.println(e);
	    }

		EbayConfig config = EbayConfig.parseConfig(keys);

		String requesturl = config.getRequestUrl("AddItem");

		ClientCredentialHandler.setDevId(config.getDevid());
		ClientCredentialHandler.setAppId(config.getAppid());
		ClientCredentialHandler.setAuthCert(config.getCert());
		ClientCredentialHandler.setToken(config.getToken());

		try {
			EBayAPIInterfaceService locator = new EBayAPIInterfaceServiceLocator();
			EBayAPIInterface service = locator.geteBayAPI(new URL(requesturl));
			((EBayAPISoapBindingStub)service).setTimeout(60000);


			AddItemRequestType request = new AddItemRequestType();
			DetailLevelCodeType[] detaillevel = new DetailLevelCodeType[2];
			detaillevel[0] = DetailLevelCodeType.ReturnAll;
			detaillevel[1] = DetailLevelCodeType.ItemReturnDescription;

			request.setDetailLevel(detaillevel);
			request.setVersion(Integer.toString(config.getVersion()));

			ItemType item = new ItemType();
			item.setTitle("Ebay Java SOAP Code Examples!");
			item.setDescription("Get your Ebay Java SOAP code samples here!!!");
			item.setStartPrice(new AmountType(1.0));
			item.setCurrency(CurrencyCodeType.USD);
			item.setLocation("Boston, MA");
			item.setCountry(CountryCodeType.US);
			item.setListingDuration(new org.apache.axis.types.Token("Days_7"));



			 BuyerPaymentMethodCodeType[] arrPaymentMethods =
			      new BuyerPaymentMethodCodeType[] {
			            BuyerPaymentMethodCodeType.PaymentSeeDescription,
			            BuyerPaymentMethodCodeType.PayPal
			      };
			item.setPayPalEmailAddress("ve2@aol.com");
            item.setPaymentMethods(arrPaymentMethods);
			CategoryType category = new CategoryType();
			category.setCategoryID("14111");
			item.setPrimaryCategory(category);
			item.setQuantity(new Integer(20));
			item.setRegionID("0");
			item.setShippingDetails(getShippingDetails());

			request.setItem(item);

			AddItemResponseType response = service.addItem(request);

			ErrorType[] errors = response.getErrors();
			if (errors != null && errors.length > 0) {
				System.out.println("One or more errors occurred while registering your item:");
				for (int i=0; i<errors.length; i++) {
					System.out.println("error: " + errors[i].getShortMessage());
				}
			} else {
				System.out.println("Created item.... showing details...");
				String itemid = response.getItemID();
				if (itemid != null) {
					System.out.println("created item ID = " + itemid);
				}

				FeeType[] fees = response.getFees();

					/*
				if (fees != null) {
					for (int i=0; i<fees.length; i++) {
						FeeType fee = fees[i];
						//com.ebay.api.soap.AmountType amount = fees[i];

						System.out.println("fee #" + (i + 1) +
								" (" + fee.getName() + ") = " +
								fee.getFee().get_value());


					}

				}
				*/
			}


		} catch (MalformedURLException e) {
			System.err.println("Invalid URL: " + e.toString());
		} catch (RemoteException e) {
			System.err.println("Error accessing remote service: " + e.toString());
		} catch (javax.xml.rpc.ServiceException e) {
			System.err.println("Error accessing service: " + e.toString());
		}

	}

	private static ShippingDetailsType getShippingDetails(){
	         // construct shipping details container
	        ShippingDetailsType sd = new ShippingDetailsType();
	        sd.setShippingType(ShippingTypeCodeType.Flat);
	        ShippingServiceOptionsType st1 = new ShippingServiceOptionsType();
	        st1.setShippingService(new org.apache.axis.types.Token("UPSGround"));
	        st1.setShippingServiceAdditionalCost(new AmountType(2.0));
	        st1.setShippingServiceCost(new AmountType(10));
	        st1.setShippingServicePriority(new Integer(1));
	        st1.setShippingInsuranceCost(new AmountType(1.0));
	        sd.setShippingServiceOptions(new ShippingServiceOptionsType[]{st1});

	        return sd;
    }

	private static String printUsage() {
		return "java -cp ebaysoap.jar;$axis\\lib\\axis.jar;$axis\\lib\\jaxrpc.jar;$axis\\lib\\log4j-1.2.8.jar;" +
		"$axis\\lib\\commons-logging.jar;$axis\\lib\\commons-discovery.jar;$axis\\lib\\saaj.jar;bin\\." +
		"com.ebay.api.soap.examples.SampleAddItem";
	}
}
