import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Color, ColorPickerControl} from "@iplab/ngx-color-picker";

@Component({
  selector: 'app-color-picker',
  templateUrl: './color-picker.component.html',
  styleUrls: ['./color-picker.component.scss']
})
export class ColorPickerComponent {
  colorPickerControl: ColorPickerControl;

  @Input() set value(value: string) {
    this.colorPickerControl.setValueFrom(value);
  }
  @Output() valueChange: EventEmitter<Color> = new EventEmitter<Color>();

  constructor() {
    this.colorPickerControl = new ColorPickerControl().hidePresets();
    this.colorPickerControl.valueChanges.subscribe((changedColor: Color) => this.valueChange.emit(changedColor));
  }
}
