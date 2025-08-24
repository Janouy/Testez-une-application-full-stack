// ***********************************************
// This example namespace declaration will help
// with Intellisense and code completion in your
// IDE or Text Editor.
// ***********************************************
// declare namespace Cypress {
//   interface Chainable<Subject = any> {
//     customCommand(param: any): typeof customCommand;
//   }
// }
//
// function customCommand(param: any): void {
//   console.warn(param);
// }
//
// NOTE: You can use it like so:
// Cypress.Commands.add('customCommand', customCommand);
//
// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

/// <reference types="cypress" />
export {};

declare global {
  namespace Cypress {
    type StubSessionsOptions = {
      fixture?: string;
      index?: number;
      admin?: boolean;
      email?: string;
      password?: string;
    };

    interface Chainable {
      loginUI(options?: {
        id?: number;
        email?: string;
        password?: string;
        firstName?: string;
        lastName?: string;
        admin?: boolean;
      }): Chainable<void>;

      goTo(path: string): Chainable<void>;

      getSessionsAndOpenDetail(options?: StubSessionsOptions): Chainable<void>;

      getSessionsAndOpenEdit(options?: StubSessionsOptions): Chainable<void>;
    }
  }
}

Cypress.Commands.add('loginUI', (opts: any = {}) => {
  const user = {
    id: typeof opts.id === 'number' ? opts.id : 1,
    email: typeof opts.email === 'string' ? opts.email : 'yoga@studio.com',
    password: typeof opts.password === 'string' ? opts.password : 'test!1234',
    firstName: typeof opts.firstName === 'string' ? opts.firstName : 'Admin',
    lastName: typeof opts.lastName === 'string' ? opts.lastName : 'Admin',
    admin: typeof opts.admin === 'boolean' ? opts.admin : true,
  };

  cy.intercept('POST', '/api/auth/login', {
    statusCode: 200,
    body: {
      id: user.id,
      username: user.firstName,
      firstName: user.firstName,
      lastName: user.lastName,
      admin: user.admin,
    },
  }).as('login');

  cy.visit('/login');
  cy.get('input[formControlName=email]').type(user.email);
  cy.get('input[formControlName=password]').type(user.password + '{enter}{enter}');
});

Cypress.Commands.add('getSessionsAndOpenDetail', (opts: any = {}) => {
  const {
    fixture = 'sessions.json',
    index = 0,
    admin = true,
    email = 'admin@example.com',
    password = 'password',
  } = opts;

  cy.intercept('GET', '/api/session', { statusCode: 200, fixture }).as('getSessions');
  cy.loginUI({ email, password, admin });
  cy.wait('@getSessions');

  cy.fixture(fixture).then((sessions) => {
    const session = sessions[index];
    cy.intercept('GET', `/api/session/${session.id}`, { statusCode: 200, body: session }).as(
      'getSessionDetail',
    );

    cy.get('[data-testid="session-item"]')
      .eq(index)
      .within(() => {
        cy.get('[data-testid="detail-button"]').click();
      });

    cy.location('pathname').should('eq', `/sessions/detail/${session.id}`);
    cy.wait('@getSessionDetail');
    cy.wrap(session).as('currentSession');
  });
});

Cypress.Commands.add('getSessionsAndOpenEdit', (opts: any = {}) => {
  const {
    fixture = 'sessions.json',
    index = 0,
    admin = true,
    email = 'admin@example.com',
    password = 'password',
  } = opts;

  cy.intercept('GET', '/api/session', { statusCode: 200, fixture }).as('getSessions');
  cy.loginUI({ email, password, admin });
  cy.wait('@getSessions');

  cy.fixture(fixture).then((sessions) => {
    const session = sessions[index];
    cy.intercept('GET', `/api/session/${session.id}`, { statusCode: 200, body: session }).as(
      'getSessionEdit',
    );

    cy.get('[data-testid="session-item"]')
      .eq(index)
      .within(() => {
        cy.get('[data-testid="edit-button"]').click();
      });

    cy.location('pathname').should('eq', `/sessions/update/${session.id}`);
    cy.wait('@getSessionEdit');
    cy.wrap(session).as('currentSession');
  });
});
