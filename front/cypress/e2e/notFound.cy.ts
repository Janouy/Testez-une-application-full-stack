/// <reference types="cypress" />
import { fakeUser } from '../support/faker';

describe('NotFound spec', () => {
  before(() => {
    cy.loginUI({
      email: fakeUser.email,
      password: fakeUser.password,
    });
  });

  it('should display the 404 page when navigating to an unknown route', () => {
    cy.visit('/route-inexistante', { failOnStatusCode: false });
    cy.contains('Page not found !').should('be.visible');
    cy.get('app-not-found').should('exist');
  });
});
