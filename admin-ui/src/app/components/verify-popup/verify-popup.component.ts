import {Component, EventEmitter, Input, Output} from '@angular/core';

@Component({
  selector: 'app-verify-popup',
  templateUrl: './verify-popup.component.html',
  styleUrls: ['./verify-popup.component.scss']
})
export class VerifyPopupComponent {

  @Input() open: boolean = false;
  @Output() openChange: EventEmitter<boolean> = new EventEmitter<boolean>();

  @Output() responded: EventEmitter<boolean> = new EventEmitter<boolean>();

  approve(): void {
    this.responded.emit(true);
    this.openChange.emit(false);
  }

  decline(): void {
    this.responded.emit(false);
    this.openChange.emit(false);
  }
}
