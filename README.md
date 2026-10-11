# Crypto Transfer Optimizer

A Spring Boot REST API that estimates how much cryptocurrency a user will receive 
after purchasing it on Bynex and transferring it to Bybit

The project demonstrates:
- integration with external cryptocurrency services
- parsing complex JSON responses
- implementation of business logic for transfer calculations
- estimation of trading and withdrawal fees

# Features
### Bynex
- Retrieve market data for a selected trading pair
- Retrieve and parse withdrawal fees from JSON
- Calculate trading commission
- Calculate withdrawal losses for each supported blockchain network
- Handle external service failures gracefully
### Bybit
- Retrieve market price for a selected coin against USDT

# Business Rules
- Only cryptocurrencies supported by Bynex can be requested
- The request must contain:
  - fiat currency name
  - cryptocurrency name 
  - purchase amount
- The purchase amount must be greater than zero
- Currently, only TAKER orders are supported

## Example Request
```http
POST http://localhost:8080/calculate
```

```json
{
    "fiatName": "USD",
    "coinName": "BTC",
    "amount": 100
}
```

# Technologies
- Java 21
- Spring Boot
- Spring WebFlux (WebClient)
- Jackson
- Jsoup
- Lombok
- Maven
- JUnit 5
- Mockito