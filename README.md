# E-Clearing client

Client for [www.e-clearing.net](http://www.e-clearing.net) written on Scala

## Includes

    * Wsdl v0.2 generated client and bean classes with help of [scalaxb](http://scalaxb.org)

    * Service-like trait to manipulate with Cards
        ```scala
            trait EclearingApi {
                def cards(): Seq[Card]
                def addCard(card: Card)
                def removeCard(card: Card)
            }
        ```