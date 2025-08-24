/// <reference types="cypress" />
import { fakeUser } from '../support/faker';

describe('Register spec', () => {
  it('Register successful', () => {
    cy.visit('/register');

    cy.intercept('POST', '/api/auth/register', {
      statusCode: 201,
      body: {},
    }).as('register');

    cy.get("[data-cy='firstName']").type(fakeUser.firstName);
    cy.get("[data-cy='lastName']").type(fakeUser.lastName);
    cy.get("[data-cy='email']").type(fakeUser.email);
    cy.get("[data-cy='password']").type(fakeUser.password);

    cy.get('button[type=submit]').click();

    cy.wait('@register');
    cy.url().should('include', '/login');
  });

  it('Submit button is desabled if a required field is missing', () => {
    cy.visit('/register');

    cy.get('[data-cy=firstName]').clear();
    cy.get('[data-cy=lastName]').type('nom');
    cy.get('[data-cy=email]').type('abc');
    cy.get('[data-cy=password]').type('azerty');

    cy.get('button[type="submit"]').should('be.disabled');
  });
});
