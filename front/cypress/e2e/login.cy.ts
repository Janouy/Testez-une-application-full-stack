/// <reference types="cypress" />
import { fakeUser } from '../support/faker';

describe('Login spec', () => {
  it('Login successfull', () => {
    cy.loginUI({
      email: fakeUser.email,
      password: fakeUser.password,
    });
  });

  it('Login fails with wrong credentials', () => {
    const wrongUser = { email: fakeUser.email, password: 'wrongpassword' };

    cy.intercept({ method: 'POST', url: '/api/auth/login*' }, (req) => {
      expect(req.body).to.deep.equal(wrongUser);
      req.reply({ statusCode: 401, body: { error: 'Invalid credentials' } });
    }).as('login');

    cy.visit('/login');
    cy.get('input[formControlName=email]').clear().type(wrongUser.email);
    cy.get('input[formControlName=password]').clear().type(wrongUser.password);
    cy.get('button[type="submit"]').click();

    cy.wait('@login');
    cy.contains('An error occurred').should('exist');
    cy.url().should('include', '/login');
  });

  it('Submit button is desabled if a required field is missing', () => {
    cy.visit('/login');

    cy.get('[data-cy=email]').clear();
    cy.get('[data-cy=password]').type('azerty');

    cy.get('button[type="submit"]').should('be.disabled');
  });
});
