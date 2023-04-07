import { Pipe, PipeTransform } from '@angular/core';
import {LightConfigDTO} from "../../dtos/scene/LightConfigDTO";

@Pipe({
  name: 'brightnessConvert'
})
export class BrightnessConvertPipe implements PipeTransform {
  transform(value: LightConfigDTO): string {
    return `${value.brightness / 100}`;
  }
}
