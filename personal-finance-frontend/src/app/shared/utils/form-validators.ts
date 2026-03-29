import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function passwordMatchValidator(passwordKey: string, confirmKey: string): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const password = group.get(passwordKey)?.value;
    const confirm = group.get(confirmKey)?.value;
    return password === confirm ? null : { passwordMismatch: true };
  };
}

export function noWhitespaceValidator(control: AbstractControl): ValidationErrors | null {
  const value: string = control.value ?? '';
  return value.trim().length === 0 && value.length > 0 ? { whitespace: true } : null;
}

export function positiveNumberValidator(control: AbstractControl): ValidationErrors | null {
  const value = Number(control.value);
  return isNaN(value) || value <= 0 ? { notPositive: true } : null;
}

