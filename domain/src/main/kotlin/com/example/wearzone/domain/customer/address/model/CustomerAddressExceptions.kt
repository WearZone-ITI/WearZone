package com.example.wearzone.domain.customer.address.model

class ShopifyCustomerIdUnavailableException :
    IllegalStateException("Shopify customer id is not available yet.")

class CannotDeleteDefaultAddressException :
    IllegalStateException("Cannot delete the customer's default address.")

class AddressPermissionException :
    IllegalStateException("Shopify address permission is not available.")

class ShopifyCustomerNotFoundException :
    IllegalStateException("Shopify customer was not found.")

class InvalidShopifyAddressException :
    IllegalStateException("Shopify rejected the address.")

class AddressResponseParseException :
    IllegalStateException("Address response could not be parsed.")

class AddressNetworkException :
    IllegalStateException("Address network error.")

class AddressLookupUnavailableException :
    IllegalStateException("Address lookup is unavailable.")

class AddressLookupTokenMissingException :
    IllegalStateException("Mapbox public access token is missing or invalid.")

class CurrentLocationPermissionDeniedException :
    IllegalStateException("Location permission was denied.")

class CurrentLocationUnavailableException :
    IllegalStateException("Current location is unavailable.")
