import { FormControl, FormGroup } from '@angular/forms';
import {
  passwordMatchValidator,
  noWhitespaceValidator,
  positiveNumberValidator,
} from './form-validators';

describe('form-validators', () => {
  describe('passwordMatchValidator', () => {
    const buildGroup = (password: string, confirm: string) =>
      new FormGroup(
        {
          password: new FormControl(password),
          confirmPassword: new FormControl(confirm),
        },
        { validators: passwordMatchValidator('password', 'confirmPassword') },
      );

    it('returns null when passwords match', () => {
      expect(buildGroup('abc123', 'abc123').errors).toBeNull();
    });

    it('returns passwordMismatch error when passwords differ', () => {
      expect(buildGroup('abc123', 'xyz').errors).toEqual({ passwordMismatch: true });
    });
  });

  describe('noWhitespaceValidator', () => {
    it('returns null for normal text', () => {
      expect(noWhitespaceValidator(new FormControl('hello'))).toBeNull();
    });

    it('returns whitespace error for all-spaces string', () => {
      expect(noWhitespaceValidator(new FormControl('   '))).toEqual({ whitespace: true });
    });

    it('returns null for empty string (required handles that)', () => {
      expect(noWhitespaceValidator(new FormControl(''))).toBeNull();
    });
  });

  describe('positiveNumberValidator', () => {
    it('returns null for positive number', () => {
      expect(positiveNumberValidator(new FormControl(10))).toBeNull();
    });

    it('returns error for zero', () => {
      expect(positiveNumberValidator(new FormControl(0))).toEqual({ notPositive: true });
    });

    it('returns error for negative', () => {
      expect(positiveNumberValidator(new FormControl(-5))).toEqual({ notPositive: true });
    });

    it('returns error for NaN', () => {
      expect(positiveNumberValidator(new FormControl('abc'))).toEqual({ notPositive: true });
    });
  });
});

