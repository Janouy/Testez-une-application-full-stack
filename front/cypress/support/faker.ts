// cypress/e2e/register.cy.js
import { faker } from '@faker-js/faker';

export const fakeUser = {
  id: faker.random.numeric(),
  firstName: faker.name.firstName(),
  lastName: faker.name.lastName(),
  email: faker.internet.email(),
  password: faker.internet.password(),
};
