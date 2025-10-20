<template>
    <div class="location-selector">
        <el-cascader
            v-model="selectedLocation"
            :options="locationOptions"
            :props="props"
            placeholder="请选择所在地"
            clearable
            filterable
            @change="handleChange"
            class="location-cascader"
        />
    </div>
</template>

<script>
import { regionData } from 'element-china-area-data'

export default {
    name: 'LocationSelector',
    props: {
        modelValue: {
            type: Array,
            default: () => []
        },
        placeholder: {
            type: String,
            default: '请选择所在地'
        }
    },
    emits: ['update:modelValue', 'change', 'locationText'],
    data() {
        return {
            selectedLocation: this.modelValue,
            locationOptions: regionData,
            props: {
                expandTrigger: 'hover',
                value: 'value',
                label: 'label',
                children: 'children'
            }
        }
    },
    watch: {
        modelValue: {
            handler(newVal) {
                this.selectedLocation = newVal;
            },
            immediate: true
        }
    },
    methods: {
        handleChange(value) {
            this.$emit('update:modelValue', value);
            this.$emit('change', value);

            // 如果需要获取完整的地区名称
            if (value && value.length > 0) {
                const locationText = this.getLocationText(value);
                this.$emit('locationText', locationText);
            } else {
                this.$emit('locationText', '');
            }
        },

        getLocationText(value) {
            const findLabel = (options, targetValue) => {
                for (const option of options) {
                    if (option.value === targetValue) {
                        return option.label;
                    }
                    if (option.children && option.children.length > 0) {
                        const found = findLabel(option.children, targetValue);
                        if (found) return found;
                    }
                }
                return null;
            };

            const labels = value.map(val => findLabel(this.locationOptions, val)).filter(Boolean);
            return labels.join(' ');
        },

        // 获取当前选中的完整地区名称
        getCurrentLocationText() {
            return this.getLocationText(this.selectedLocation);
        }
    }
}
</script>

<style scoped>
.location-selector {
    display: inline-block;
}

.location-cascader {
    width: 100%;
}

/* 如果需要自定义样式 */
.location-cascader :deep(.el-input__wrapper) {
    border-radius: 4px;
}

.location-cascader :deep(.el-cascader) {
    width: 100%;
}
</style>
