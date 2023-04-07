import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";
import {Observable, of} from "rxjs";
import {Color, ColorPickerControl} from "@iplab/ngx-color-picker";
import {Rgba} from "@iplab/ngx-color-picker/lib/helpers/rgba.class";

@Component({
  selector: 'app-light-config',
  templateUrl: './light-config.component.html',
  styleUrls: ['./light-config.component.scss']
})
export class LightConfigComponent implements OnInit{

  @Input() lightConfig!: LightConfigDTO;
  @Input() index!: number;
  editorOpen: boolean = false;

  @Input() openEditorId$: Observable<number> = of(-1);
  @Output() requestToggleOpen: EventEmitter<boolean> = new EventEmitter<boolean>();

  colorPickerControl: ColorPickerControl;
  private initialLightConfig: LightConfigDTO = this.lightConfig;

  constructor() {
    this.colorPickerControl = new ColorPickerControl().hidePresets();
  }

  ngOnInit() {
    this.initialLightConfig = structuredClone(this.lightConfig);

    this.openEditorId$.subscribe(index => {
      this.editorOpen = !this.editorOpen;
      this.editorOpen = index === this.index && this.editorOpen;
    });

    this.setColorPickerValue(this.lightConfig);
    this.colorPickerControl.valueChanges.subscribe((change: Color) => {
      const color: Rgba = change.getRgba();
      this.lightConfig.red = color.getRed();
      this.lightConfig.green = color.getGreen();
      this.lightConfig.blue = color.getBlue();
      this.lightConfig.brightness = color.getAlpha() * 100;
      this.lightConfig = {... this.lightConfig}; // to let the template know that stuff has changed
    });
  }

  toggleOpen() {
    this.requestToggleOpen.next(!this.editorOpen);
  }

  rename(newName: string) {
    this.lightConfig.name = newName;
  }

  saveChanges(): void {
    console.warn("not yet implemented");
  }

  reset(): void {
    this.lightConfig = structuredClone(this.initialLightConfig);
    this.setColorPickerValue(this.lightConfig);
  }

  private setColorPickerValue(config: LightConfigDTO) {
    this.colorPickerControl.setValueFrom(
      `rgba(${config.red}, ${config.green}, ${config.blue}, ${config.brightness / 100})`
    );
  }
}
