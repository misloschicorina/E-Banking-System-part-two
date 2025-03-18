MISLOSCHI ALEXANDRA CORINA 324 CA - PROIECT ETAPA 2

# E-Banking System

## Introduction

This project implements a comprehensive e-banking system in Java, simulating advanced 
banking functionalities for users. The system integrates features for managing accounts,
transactions, and financial tools while introducing enhanced capabilities like business 
accounts, cashback strategy, and customizable service plans.

## Core Concepts

The system revolves around managing users, accounts, and transactions. 
Here's how each element fits together:

## Users
Each user in the system is identified by personal details including their first name, 
last name, email address, date of birth, occupation, and service plan. The service plan 
can be one of the following: standard, student, silver, or gold, depending on the user's
preferences and eligibility.

## Accounts
A user can have multiple accounts, and each account can be of one of three types:

- **Classic Account**: Standard account type with basic functionalities like deposit, 
    withdrawal, and transfer.
- **Savings Account**: Account that generates interest over time and has additional 
    features, such as setting and modifying interest rates.
- **Business Account**: Shared account type for entrepreneurs, with role-based access
    (owner, manager, employee) and features like spending and deposit limits.

## Cards
Accounts can have associated cards. The system supports two types of cards:

- **Classic Cards**: Regular cards for standard transactions.
- **One-time Cards**: Cards that are designed for a single transaction. Once used, they
    are automatically deleted from the system and a new card must be regenerated for 
    future transactions.

## Transactions
Every action taken by a user that affects their account is recorded as a transaction. 
These transactions are stored in ascending order by timestamp and can be printed 
using the `printTransactions` command. This command will provide a complete history 
of a user's transactions.

## Exchange Rates
Since banks often deal with multiple currencies, exchange rates will be stored to
enable currency conversions between different account types. The exchange rate will 
be specified between a "from" and a "to" currency, along with the corresponding rate.

## Cashback
Each merchant introduces a cashback strategy to attract more customers. The cashback 
is calculated separately for each user account and operates under the following plan:

### Spending Threshold

The cashback depends on the user's service plan and the total amount spent:

- **For 100 RON spent**:
  - 0.1% cashback for Standard and Student plans
  - 0.3% cashback for Silver plan
  - 0.5% cashback for Gold plan
- **For 300 RON spent**:
  - 0.2% cashback for Standard and Student plans
  - 0.4% cashback for Silver plan
  - 0.55% cashback for Gold plan
- **For 500 RON spent**:
  - 0.25% cashback for Standard and Student plans
  - 0.5% cashback for Silver plan
  - 0.7% cashback for Gold plan

Cashback is applied to the current transaction once one of these thresholds is met. The 
total spending is tracked per account for all merchants with this type of cashback strategy.

## Service Plans

Users can choose from the following service plans:

- **Standard**: A basic plan with a 0.2% transaction fee on all transactions.
- **Student**: A plan specifically designed for students with no transaction fees.
- **Silver**: No transaction fees for transactions under 500 RON. A 0.1% fee is applied 
    for transactions above 500 RON.
- **Gold**: A premium plan with no transaction fees.

Upgrades to higher service plans can be performed through specific transactions:

- **Standard/Student to Silver**: Requires a 100 RON fee.
- **Silver to Gold**: Requires a 250 RON fee.
- **Standard/Student to Gold**: Requires a 350 RON fee.

All upgrade fees are deducted from the user's account in RON. Once upgraded, all associated 
accounts inherit the new plan benefits.

## Business Accounts

Business accounts support advanced management features:

### Roles
- **Owner**: Full permissions for managing the account, transactions, and associated users.
- **Manager**: Can manage transactions and cards but with fewer permissions than the owner.
- **Employee**: Limited permissions, restricted by predefined spending and deposit limits.

### Features
- Transactions and permissions are role-specific, ensuring clear access control.
- **Business Reports**: Summarize account activity, providing insights by user or merchant.
  - **Transaction Report**: Displays total spending and deposits for each user.
  - **Merchant Report**: Highlights spending per merchant and lists the users who interacted 
    with them.

## Commands Implemented

Below is a description of each implemented command:

### Account Management Commands

- **`addAccount`**: Creates a new account for a user. Supports classic, savings, or 
business account types.
- **`deleteAccount`**: Deletes an account. Only the owner can delete business accounts.
- **`setMinBalance`**: Sets a minimum balance for an account, restricting transactions if 
the balance goes below the set limit.
- **`withdrawSavings`**: Allows a user to transfer funds from a savings account to a classic 
account. This action requires the user to meet the age requirement (21 or older).
- **`addNewBusinessAssociate`**: Adds a manager or employee to a business account, defining 
their role.
- **`changeSpendingLimit`**: Updates the spending limit for employees on a business account.
- **`changeDepositLimit`**: Updates the deposit limit for employees on a business account.

### Card Management Commands

- **`createCard`**: Creates a card for a user's account. For business accounts, the email of the 
creator is logged.
- **`createOneTimeCard`**: Creates a single-use card for a user's account.
- **`deleteCard`**: Deletes a card. Employees can only delete their own cards, while managers and
owners can delete any card.

### Transactions Commands

- **`addFunds`**: Adds funds to a user's account. Business account limits are respected based on roles 
(employee, manager, or owner).
- **`payOnline`**: Processes online payments. Can handle invalid merchants, and cashback is applied if 
applicable. A transaction fee is charged based on the user's service plan.
- **`sendMoney`**: Transfers funds between accounts or to merchants. For merchant transactions, only the 
specified amount is deducted, converted if necessary. A transaction fee is charged based on the user's 
service plan.
- **`cashWithdrawal`**: Allows users to withdraw cash from ATMs in RON. Fees are applied based on the 
service plan.
- **`splitPayment`**: Supports equal or custom splitting of payments among multiple users. Each 
participant must accept the transaction before processing.
- **`acceptSplitPayment`**: Accepts a pending split payment request.
- **`rejectSplitPayment`**: Rejects a pending split payment request, canceling the transaction 
for all participants.
- **`upgradePlan`**: Upgrades a user's service plan. Fees are applied based on the desired plan level.
- **`setAlias`**: Assigns an alias to an account for easier identification.
- **`addInterest`**: Adds interest to a savings account.
- **`changeInterestRate`**: Updates the interest rate for a savings account.
- **`checkCardStatus`**: Checks the current status of a card (e.g., active, frozen, or blocked).

### Reporting Commands

- **`printUsers`**: Displays a list of all users, including their accounts and associated cards.
- **`printTransactions`**: Lists all transactions for a specific user.
- **`report`**: Generates detailed reports of transactions for a specified timeframe.
- **`spendingsReport`**: Summarizes the spending activities from an account within a given period.
- **`businessReport`**: Provides reports on business accounts, focusing on transactions or merchant 
activities.

These commands are part of the system's functionality and are mapped to specific 
user actions to simulate typical banking operations. Each command has a defined behavior 
to ensure the proper handling of transactions, account management, and reporting.

## Design Patterns Used

### 1. Factory Method (for Cards)

- **Where Used**:
  - For creating `Card` and `OneTimeCard` objects in the `createCard` and `createOneTimeCard` methods.
- **Motivations**:
  - **Separation of Responsibilities**: Decouples instantiation logic from the rest of the system, 
  improving organization and maintainability.
  - **Extensibility**: Enables easy addition of new card or account types (e.g., `BusinessCard`, 
`GoldCard`) without modifying existing code, adhering to the Open/Closed Principle.
  - **Testability**: Factories can be tested independently to ensure correct object creation.

### 2. Factory Method (for Accounts)

- **Where Used**:
  - For creating the three types of accounts: `ClassicAccount`, `SavingsAccount`, and `BusinessAccount`.
- **Motivations**:
  - **Separation of Responsibilities**: Decouples instantiation logic for accounts from the rest of 
  the system.
  - **Extensibility**: Allows adding new account types (e.g., `PremiumAccount`) without modifying 
  existing code, adhering to the Open/Closed Principle.
  - **Testability**: Factories can be independently tested to ensure correct creation of account 
  objects.

### 3. Strategy Pattern

- **Where Used**:
  - In the `splitPayment` method to select and use strategies such as `EqualSplitStrategy` and 
  `CustomSplitStrategy`.
- **Motivations**:
  - **Separation of Responsibilities**: Delegates specific splitting logic to strategy classes, 
  simplifying the `splitPayment` method.
  - **Extensibility**: Allows easy addition of new split types without modifying existing code.
  - **Flexibility at Runtime**: Enables dynamic selection of strategies based on user input.
  - **Encapsulation of Behaviors**: Each splitting logic is encapsulated in its own class, making
  the code more modular and robust.

### 4. Command Pattern

- **Where Used**:
  - For all commands in the system.
- **Motivations**:
  - **Encapsulation**: Encapsulates request details into objects, decoupling request handling from 
  the request execution.
  - **Extensibility**: New commands can be added without changing existing code, adhering to the
  Open/Closed Principle.
 - **Modular Design**: Each command operates independently, simplifying testing and debugging.